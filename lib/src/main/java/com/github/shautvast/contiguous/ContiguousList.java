package com.github.shautvast.contiguous;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

//notes:
// should find out growth factor of arraylist
// investigate data array reuse (pooling, SoftReferences etc)
// is all this inner class usage not wasteful? (references to this etc)

/**
 * Short for Contiguous Layout List, an Experimental List implementation
 * Behaves like an ArrayList in that it's resizable and indexed.
 * The difference is that it uses an efficiently dehydrated version of the object in a  cpu cache friendly, contiguous storage in a bytearray,
 * without object instance overhead.
 * <p>
 * Only uses reflection api on creation of the list (and in the get() method, but the end user should employ value
 * iteration rather than element iteration using said get method).
 * Adding/Retrieving/Deleting depend on VarHandles and are aimed to be O(L) runtime complexity
 * where L is the nr of attributes to get/set from the objects (recursively).So O(1) for length of the list
 * <p>
 * The experiment is to see if performance gains from the memory layout make up for this added overhead
 * <p>
 * Employs the SQLite style of data storage, most notably integer numbers are stored with variable byte length
 * <p>
 * The classes stored in {@link ContiguousList} MUST have a no-args constructor. (so sorry)
 * // There is (was?) a way to create instances without a no-args constructor. I have used it somewhere in the past...
 * // I think {@link java.io.ObjectInputStream} uses it.
 * <p>
 * Like ArrayList, mutating operations are not synchronized.
 * <p>
 * Does not allow null elements.
 * <p>
 * Implements java.util.List but some methods are not (yet) implemented mainly because they don't make much sense
 * performance-wise, like the indexed add and set methods. They mess with the memory layout. The list is meant to
 * be appended at the tail.
 * <p>
 * What I think is a potential use case is a simple CRUD application.
 * Here it would become possible to skip Object (when reading from the database),
 * and directly map the results to JSON. Both writing and reading should ideally be faster
 * than doing it the regular way.
 */
public class ContiguousList<E> extends NotImplementedList<E> implements List<E> {

    private static final byte[] DOUBLE_TYPE = {7};
    private static final byte[] FLOAT_TYPE = {10}; // not in line with SQLite anymore
    private static final int STRING_OFFSET = 13;
    private static final int BYTES_OFFSET = 12; // blob TODO decide if include
    public static final int MAX_24BITS = 8388607;
    public static final long MAX_48BITS = 140737488355327L;

    /*
     * storage for dehydated objects
     */
    private ByteBuffer data = BufferCache.get(4096);

    private int bufferPosition;

    private final ArrayList<Integer> elementIndices = new ArrayList<>(); // avoids autoboxing. Could also use standard ArrayList though
    // is there a standard lib IntList??

    private int size;

    private final TypeHandler rootHandler;
    private static final Map<Class<?>, TypeHandler> TYPE_HANDLERS = new HashMap<>();

    public ContiguousList(Class<E> type) {
        this.rootHandler = inspectType(type);
        elementIndices.add(0); // index of first element
    }

    public void close(){
        BufferCache.release(this.data);
    }

    /*
     * Get a list of setters and getters to execute later on to get/set the values of the object
     *
     * The order of excution is crucial, ie MUST be the same as the order in the stored data.
     *
     * The advantage of the current implementation is that the binary data is not aware of the actual
     * object graph. It only knows the 'primitive' values.
     */
    private TypeHandler inspectType(Class<?> elementClass) {
        return TYPE_HANDLERS.computeIfAbsent(elementClass, k ->
                PropertyHandlerFactory.forType(elementClass)
                        .orElseGet(() -> {
                            TypeHandler compoundTypeHandler = new CompoundTypeHandler(elementClass);
                            addPropertyHandlersForCompoundType(elementClass, (CompoundTypeHandler) compoundTypeHandler);
                            return compoundTypeHandler;
                        })
        );
    }

    /*
     * using reflection find all properties in the element, recursing down when the property is compound
     */
    private void addPropertyHandlersForCompoundType(Class<?> type, CompoundTypeHandler parentCompoundType) {
        final MethodHandles.Lookup lookup = getLookup(type);
        Arrays.stream(type.getDeclaredFields())
                .forEach(field -> {
                    try {
                        Class<?> fieldType = field.getType();
                        MethodHandle getter = lookup.findGetter(type, field.getName(), fieldType);
                        MethodHandle setter = lookup.findSetter(type, field.getName(), fieldType);

                        Optional<TypeHandler> typeHandler = PropertyHandlerFactory.forType(fieldType, field.getName(), getter, setter);
                        if (typeHandler.isPresent()) {
                            parentCompoundType.addHandler(field.getName(), (BuiltinTypeHandler<?>) typeHandler.get());
                        } else {
                            CompoundTypeHandler handler = new CompoundTypeHandler(fieldType, field.getName());
                            handler.setGetter(getter);
                            handler.setSetter(setter);
                            parentCompoundType.addChild(field, handler);

                            addPropertyHandlersForCompoundType(fieldType, handler);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static MethodHandles.Lookup getLookup(Class<?> type) {
        try {
            return MethodHandles.privateLookupIn(type, MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("Contract")
    public boolean add(E element) {
        if (element == null) {
            return false;
        }
        storePropertyData(element, rootHandler);
        extend();
        return true;
    }

    /*
     * Stores the properties
     *
     * walks the object graph depth-first. The order of the stored data is crucial, because only
     * leaf elements are stored and all information on what object owns what is implicit.
     */
    private void storePropertyData(Object element, TypeHandler typeHandler) {
        if (typeHandler instanceof BuiltinTypeHandler<?>) {
            ((BuiltinTypeHandler<?>) typeHandler).storePropertyValue(element, this);
        } else {
            // passed type is compound ie. has child properties
            ((CompoundTypeHandler) typeHandler).getProperties().forEach(property -> {
                if (property instanceof BuiltinTypeHandler<?>) {
                    ((BuiltinTypeHandler<?>) property).storePropertyValue(element, this);
                } else {
                    CompoundTypeHandler child = ((CompoundTypeHandler) property);
                    try {
                        Object result = child.getGetter().invoke(element);
                        storePropertyData(result, child);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    /**
     * Get element at specified index.
     * <p>
     * Please note that this creates a new instance using the stored element data and is therefore
     * not the recommended usecase because of the performance penalty. It's implemented for convenience, but
     * there are alternatives such as the propertyIterator and getXX...[implement]
     *
     * @param index The index of the element data
     * @return a new element instance
     */
    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index <0 or >" + size);
        }
        data.position(elementIndices.get(index));
        try {
            if (rootHandler instanceof BuiltinTypeHandler<?>) {
                Object read = ValueReader.read(data);
                return (E) ((BuiltinTypeHandler<?>) rootHandler).cast(read);
            }
            // create a new instance of the list element type
            E newInstance = (E) rootHandler.getType().getDeclaredConstructor().newInstance();

            // set the data
            copyDataIntoNewObject(newInstance, (CompoundTypeHandler) rootHandler);

            return newInstance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    /*
     *
     */
    private void copyDataIntoNewObject(Object element, CompoundTypeHandler compoundType) {
        compoundType.getProperties().forEach(property -> {
            if (property instanceof BuiltinTypeHandler) {
                BuiltinTypeHandler<?> type = ((BuiltinTypeHandler<?>) property);
                type.setValue(element, ValueReader.read(data));
            } else {
                try {
                    CompoundTypeHandler p = (CompoundTypeHandler) property;
                    // create a new instance of the property
                    Object newInstance = p.getType().getDeclaredConstructor().newInstance();

                    // set it on the parent
                    p.getSetter().invokeWithArguments(element, newInstance);

                    // recurse down
                    copyDataIntoNewObject(newInstance, p);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Returns an {@link Iterator} over the property values in the List. That is, it iterates all
     * bean property values in a fixed order for all elements.
     * <p>
     * Because the values differ in type the output type is Object
     * <p>
     * It detects {@link ConcurrentModificationException} if the underlying list was updated while iterating.
     * <p>
     *
     * @return an Iterator<?>
     */
    public Iterator<?> valueIterator() {
        return new ValueIterator();
    }

    public Iterator<String> jsonIterator() {
        return new JsonIterator();
    }

    class JsonIterator implements Iterator<String> {

        private int index;

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public String next() {
            return getAsJson(index++);
        }
    }

    /**
     * For now the simplest thing I can think of to get this to work
     *
     * @param index
     * @return
     */
    public String getAsJson(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index <0 or >" + size);
        }
        data.position(elementIndices.get(index));
        if (rootHandler instanceof BuiltinTypeHandler<?>) {
            BuiltinTypeHandler<?> handler = (BuiltinTypeHandler<?>) rootHandler;
            return getValue(handler);
        }
        // create a new instance of the list element type
        StringBuilder s = new StringBuilder(300);
        s.append("{");
        copyDataIntoStringBuilder(s, (CompoundTypeHandler) rootHandler);
        s.append("}");
        return s.toString();
    }

    private String getValue(BuiltinTypeHandler<?> handler) {
        String value = String.valueOf(ValueReader.read(data));
        if (handler instanceof StringHandler) {
            return quote(value);
        }
        return value;
    }

    private static String quote(String out) {
        StringBuilder s = new StringBuilder(out.length() + 2);
        out = s.append("\"").append(out).append("\"").toString();
        return out;
    }

    /*
     *
     */
    private void copyDataIntoStringBuilder(StringBuilder s, CompoundTypeHandler compoundType) {
        compoundType.getProperties().forEach(property -> {
            if (property instanceof BuiltinTypeHandler) {
                BuiltinTypeHandler<?> typeHandler = (BuiltinTypeHandler<?>) property;
                s.append("\"")
                        .append(typeHandler.getName())
                        .append("\": ")
                        .append(getValue(typeHandler));
            } else {
                CompoundTypeHandler p = (CompoundTypeHandler) property;
                s.append(p.getName()).append(":{");
                // recurse down
                copyDataIntoStringBuilder(s, p);
                s.append("}");
            }
            s.append(", ");
        });
        s.setLength(s.length() - 2);
    }

    List<BuiltinTypeHandler<?>> getBuiltinTypeHandlers() {
        final List<BuiltinTypeHandler<?>> types = new ArrayList<>();
        getStoredTypes(rootHandler, types);
        return types;
    }

    private void getStoredTypes(TypeHandler handler, List<BuiltinTypeHandler<?>> types) {
        if (handler instanceof BuiltinTypeHandler<?>) {
            types.add((BuiltinTypeHandler<?>) handler);
        } else {
            ((CompoundTypeHandler) handler).getProperties()
                    .forEach(propertyHandler -> getStoredTypes(propertyHandler, types));
        }
    }

    public class ValueIterator implements Iterator<Object> {
        private final int originalSize;
        private final int originalPosition;
        private final List<BuiltinTypeHandler<?>> typeHandlers;
        private Iterator<BuiltinTypeHandler<?>> typeHandlersIterator;

        ValueIterator() {
            typeHandlers = getBuiltinTypeHandlers();
            typeHandlersIterator = typeHandlers.iterator();
            this.originalSize = size;
            this.originalPosition = data.position();
            data.position(0);
        }

        @Override
        public boolean hasNext() {
            return data.position() < originalPosition;
        }

        @Override
        public Object next() {
            if (originalSize != size) {
                throw new ConcurrentModificationException("Modifications detected while iterating.");
            }
            /* The following depends on the bytebuffer position. Calling add(..) would mess it up
             * so that's why we first check for modifications (me and the computer)
             *
             * I could also maintain the position here. But you main want to fail ... dunno */
            Object rawValue = ValueReader.read(data);

            // transform (currently integers to the expected type)
            BuiltinTypeHandler<?> handler;
            while (!typeHandlersIterator.hasNext()) {
                typeHandlersIterator = typeHandlers.iterator();
            }
            handler = typeHandlersIterator.next();

            return handler.cast(rawValue);
        }
    }


    /**
     * Allows 'iterating insertion of data'. Returns an iterator of Setter
     * Does not work for compound types yet // (check if still valid)
     *
     * @return an Iterator over bean property setters. Once an object is completely written,
     * call finishObject, to increase the element count and to continue with the next element.
     */
    public SetterIterator setterIterator() {
        return new SetterIterator();
    }

    public class Setter {

        private final BuiltinTypeHandler<?> currentHandler;

        public Setter(BuiltinTypeHandler<?> currentHandler) {
            this.currentHandler = currentHandler;
        }

        public String getFieldName() {
            return currentHandler.getName();
        }

        public void set(Object fieldValue) {
            currentHandler.storeValue(fieldValue, ContiguousList.this);
        }
    }

    public class SetterIterator implements Iterator<Setter> {
        private final List<Setter> properties = new ArrayList<>();
        private Iterator<Setter> currentSetterIterator;

        public SetterIterator() {
            List<BuiltinTypeHandler<?>> builtinTypeHandlers = getBuiltinTypeHandlers();
            for (BuiltinTypeHandler<?> builtinTypeHandler : builtinTypeHandlers) {
                properties.add(new Setter(builtinTypeHandler));
            }
            // what to do with compound?
            currentSetterIterator = this.properties.iterator();
        }

        @Override
        public boolean hasNext() {
            return currentSetterIterator.hasNext();
        }

        @Override
        public Setter next() {
            return currentSetterIterator.next();
        }

        /**
         * Indicates that writing of the current object is complete, so
         * that it will increment the element count and start a new property iterator
         * for the next object (or none it was the last).
         */
        public void finishObject() {
            extend();
            currentSetterIterator = properties.iterator();
        }
    }

    public boolean addAll(Collection<? extends E> collection) {
        for (E element : collection) {
            add(element);
        }
        return true;
    }

    public void clear() {
        this.bufferPosition = 0;
        this.size = 0;
    }

    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Object[] toArray() {
        Object[] objects = new Object[size];
        for (int i = 0; i < size; i++) {
            objects[i] = get(i);
        }
        return objects;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] ts) {
        if (size > ts.length) {
            return (T[]) toArray();
        }
        for (int i = 0; i < size; i++) {
            ts[i] = (T) get(i);
        }
        return ts;
    }

    /**
     * Standard Iterator that conforms to the protocol of java.util.List.
     * Although it's convenient to use, it's not the best way to read from the list because it
     * needs the Reflection API to instantiate new objects of the element type.
     * <p/>.
     *
     * @return An Iterator over the elements in the List
     */
    @Override
    public Iterator<E> iterator() {
        return new StandardElementIterator<>();
    }

    /*
     * Iterator used by the standard Iterator() method
     */
    class StandardElementIterator<F> implements Iterator<F> {

        private int curIndex = 0;

        @Override
        public boolean hasNext() {
            return curIndex < size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public F next() {
            return (F) get(curIndex++);
        }
    }

    private void store(byte[] bytes) {
        ensureFree(bytes.length);
        data.position(bufferPosition); // ensures intermittent reads/writes are safe
        data.put(bytes);
        bufferPosition += bytes.length;
    }

    private void store0() {
        ensureFree(1);
        data.put((byte) 0);
        bufferPosition += 1;
    }

    void storeString(String value) {
        if (value == null) {
            store0();
        } else {
            byte[] utf = value.getBytes(StandardCharsets.UTF_8);
            store(Varint.write(((long) (utf.length) << 1) + STRING_OFFSET));
            store(utf);
        }
    }

    void storeLong(Long value) {
        if (value == null) {
            store0();
        } else {
            byte[] valueAsBytes = getValueAsBytes(value);
            store(getIntegerType(value, valueAsBytes.length));
            store(valueAsBytes);
        }
    }

    void storeInteger(Integer value) {
        if (value == null) {
            store0();
        } else {
            byte[] valueAsBytes = getValueAsBytes(value);
            store(getIntegerType(value, valueAsBytes.length));
            store(valueAsBytes);
        }
    }

    void storeByte(Byte value) {
        if (value == null) {
            store0();
        } else {
            byte[] valueAsBytes = getValueAsBytes(value);
            store(getIntegerType(value, valueAsBytes.length));
            store(valueAsBytes);
        }
    }

    void storeShort(Short value) {
        if (value == null) {
            store0();
        } else {
            byte[] valueAsBytes = getValueAsBytes(value);
            store(getIntegerType(value, valueAsBytes.length));
            store(valueAsBytes);
        }
    }

    void storeDouble(Double value) {
        if (value == null) {
            store0();
        } else {
            store(DOUBLE_TYPE);
            store(ByteBuffer.wrap(new byte[8]).putDouble(0, value).array());
        }
    }

    void storeFloat(Float value) {
        if (value == null) {
            store0();
        } else {
            store(FLOAT_TYPE);
            store(ByteBuffer.wrap(new byte[4]).putFloat(0, value).array());
        }
    }

    // to be called by framework to force element count
    // used by SetterIterator
    void extend() {
        size += 1;
        // keep track of index of element in data
        elementIndices.add(bufferPosition);
    }

    byte[] getData() {
        return Arrays.copyOfRange(data.array(), 0, bufferPosition);
    }

    private void ensureFree(int length) {
        while (bufferPosition + length > data.capacity()) {
            ByteBuffer bytes = this.data;
            BufferCache.release(this.data);
            this.data = BufferCache.get((int)(bytes.capacity() * 1.5));
            this.data.put(bytes);
        }
    }

    private static byte[] getValueAsBytes(long value) {
        if (value == 0) {
            return new byte[0];
        } else if (value == 1) {
            return new byte[0];
        } else {
            return longToBytes(value, getLengthOfByteEncoding(value));
        }
    }

    private static byte[] getValueAsBytes(int value) {
        if (value == 0) {
            return new byte[0];
        } else if (value == 1) {
            return new byte[0];
        } else {
            return intToBytes(value, getLengthOfByteEncoding(value));
        }
    }

    private static byte[] getValueAsBytes(short value) {
        if (value == 0) {
            return new byte[0];
        } else if (value == 1) {
            return new byte[0];
        } else {
            return intToBytes(value, getLengthOfByteEncoding(value));
        }
    }

    private static byte[] getValueAsBytes(byte value) {
        if (value == 0) {
            return new byte[0];
        } else if (value == 1) {
            return new byte[0];
        } else {
            return new byte[]{value};
        }
    }

    private static int getLengthOfByteEncoding(long value) {
        long u;
        if (value < 0) {
            u = ~value;
        } else {
            u = value;
        }
        if (u <= Byte.MAX_VALUE) {
            return 1;
        } else if (u <= Short.MAX_VALUE) {
            return 2;
        } else if (u <= MAX_24BITS) {
            return 3;
        } else if (u <= Integer.MAX_VALUE) {
            return 4;
        } else if (u <= MAX_48BITS) {
            return 6;
        } else {
            return 8;
        }
    }

    private static int getLengthOfByteEncoding(short value) {
        int u;
        if (value < 0) {
            u = ~value;
        } else {
            u = value;
        }
        if (u <= Byte.MAX_VALUE) {
            return 1;
        }
        return 2;
    }

    private static int getLengthOfByteEncoding(int value) {
        int u;
        if (value < 0) {
            u = ~value;
        } else {
            u = value;
        }
        if (u <= Byte.MAX_VALUE) {
            return 1;
        } else if (u <= Short.MAX_VALUE) {
            return 2;
        } else if (u <= MAX_24BITS) {
            return 3;
        } else {
            return 4;
        }
    }

    private static byte[] intToBytes(int n, int nbytes) {
        byte[] b = new byte[nbytes];
        for (int i = 0; i < nbytes; i++) {
            b[i] = (byte) ((n >> (nbytes - i - 1) * 8) & 0xFF);
        }

        return b;
    }

    private static byte[] longToBytes(long n, int nbytes) {
        byte[] b = new byte[nbytes];
        for (int i = 0; i < nbytes; i++) {
            b[i] = (byte) ((n >> (nbytes - i - 1) * 8) & 0xFF);
        }

        return b;
    }

    private static byte[] getIntegerType(long value, int bytesLength) {
        if (value == 0) {
            return new byte[]{8};
        } else if (value == 1) {
            return new byte[]{9};
        } else {
            if (bytesLength < 5) {
                return Varint.write(bytesLength);
            } else if (bytesLength < 7) {
                return Varint.write(5);
            } else return Varint.write(6);
        }
    }
}
