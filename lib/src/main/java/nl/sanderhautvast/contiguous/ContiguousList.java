package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

//notes:
//1. should find out growth factor of arraylist
//2. elementIndices can be arrayList
/**
 * Short for Contiguous Layout List, an Experimental List implementation
 * Behaves like an ArrayList in that it's resizable and indexed.
 * The difference is that it uses an efficiently dehydrated version of the object in a  cpu cache friendly, contiguous storage in a bytearray,
 * without object instance overhead.
 * <p>
 * Only uses reflection api on creation of the list.
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
    private ByteBuffer data = ByteBuffer.allocate(32);

    private int currentElementValueIndex;

    private int[] elementIndices = new int[10];

    private int size;

    private TypeHandler rootHandler;

    public ContiguousList(Class<E> type) {
        inspectType(type);
        elementIndices[0] = currentElementValueIndex; // index of first element
    }

    /*
     * Get a list of setters and getters to execute later on to get/set the values of the object
     *
     * The order of excution is crucial, ie MUST be the same as the order in the stored data.
     *
     * The advantage of the current implementation is that the binary data is not aware of the actual
     * object graph. It only knows the 'primitive' values.
     */
    private void inspectType(Class<?> type) {
        if (PropertyHandlerFactory.isKnownType(type)) {
            this.rootHandler = PropertyHandlerFactory.forType(type);
        } else {
            CompoundTypeHandler compoundType = new CompoundTypeHandler(type);
            this.rootHandler = compoundType;
            try {
                addPropertyHandlersForCompoundType(type, compoundType);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void addPropertyHandlersForCompoundType(Class<?> type, CompoundTypeHandler parentCompoundType) throws IllegalAccessException {
        final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
        Arrays.stream(type.getDeclaredFields())
                .forEach(field -> {
                    try {
                        Class<?> fieldType = field.getType();
                        MethodHandle getter = lookup.findGetter(type, field.getName(), fieldType);
                        MethodHandle setter = lookup.findSetter(type, field.getName(), fieldType);

                        if (PropertyHandlerFactory.isKnownType(fieldType)) {
                            BuiltinTypeHandler<?> primitiveType = PropertyHandlerFactory.forType(fieldType, getter, setter);

                            parentCompoundType.addHandler(field.getName(), primitiveType);
                        } else {
                            CompoundTypeHandler newParent = new CompoundTypeHandler(fieldType);
                            newParent.setGetter(getter);
                            newParent.setSetter(setter);
                            parentCompoundType.addChild(field, newParent);

                            addPropertyHandlersForCompoundType(fieldType, newParent);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    @SuppressWarnings("Contract")
    public boolean add(E element) {
        if (element == null) {
            return false;
        }
        getProperties(element, rootHandler);
        size += 1;

        // keep track of where the objects are stored
        if (elementIndices.length < size + 1) {
            this.elementIndices = Arrays.copyOf(this.elementIndices, this.elementIndices.length * 2);
        }
        elementIndices[size] = currentElementValueIndex;
        return true;
    }

    private void getProperties(Object element, TypeHandler typeHandler) {
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
                        getProperties(result, child);
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
        data.position(elementIndices[index]);
        try {
            if (rootHandler instanceof BuiltinTypeHandler<?>) {
                Object read = ValueReader.read(data);
                return (E) ((BuiltinTypeHandler<?>) rootHandler).transform(read);
            }
            // create a new instance of the list element type
            E newInstance = (E) rootHandler.getType().getDeclaredConstructor().newInstance();

            // set the data
            setProperties(newInstance, (CompoundTypeHandler) rootHandler);

            return newInstance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private void setProperties(Object element, CompoundTypeHandler compoundType) {
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
                    setProperties(newInstance, p);
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
     *
     * NB the actual type (right now) is the `raw` value: all integers are of type Long, BigInteger is String
     * // That is unfortunate (or must I say: annoying!), but for something like JSON not a problem (I think).
     * // So maybe keep this in (say 'rawValueIterator') and also create a typesafe iterator.
     * <p>
     * It detects {@link ConcurrentModificationException} if the underlying list was updated while iterating.
     * <p>
     * // I should probably include a type so that the caller could cast to the correct type
     * // not sure yet
     *
     * @return an Iterator<?>
     */
    public Iterator<?> valueIterator() {
        return new ValueIterator();
    }

    class ValueIterator implements Iterator<Object> {
        private final int originalSize;
        private final int originalPosition;

        ValueIterator() {
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
            return ValueReader.read(data);
        }
    }

    /**
     * Returns an {@link Iterator} over the property values of the specified element in the List.
     *
     * @return
     */
    public Iterator<Object> valueIterator(int index) {
        //TODO
        return null;
    }

    public boolean addAll(Collection<? extends E> collection) {
        for (E element : collection) {
            add(element);
        }
        return true;
    }

    public void clear() {
        this.currentElementValueIndex = 0;
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
    public Iterator<E> iterator() {
        return new ElementIterator<>();
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


    class ElementIterator<F> implements Iterator<F> {

        private int curIndex = 0;
        private final int originalSize;

        ElementIterator() {
            this.originalSize = size;
            data.position(0);
        }

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
        data.position(currentElementValueIndex); // ensures intermittent reads/writes are safe
        data.put(bytes);
        currentElementValueIndex += bytes.length;
    }

    private void store0() {
        ensureFree(1);
        data.put((byte) 0);
        currentElementValueIndex += 1;
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

    byte[] getData() {
        return Arrays.copyOfRange(data.array(), 0, currentElementValueIndex);
    }

    int[] getElementIndices() {
        return Arrays.copyOfRange(elementIndices, 0, size + 1);
    }

    private void ensureFree(int length) {
        while (currentElementValueIndex + length > data.capacity()) {
            byte[] bytes = this.data.array();
            this.data = ByteBuffer.allocate(this.data.capacity() * 2);
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
