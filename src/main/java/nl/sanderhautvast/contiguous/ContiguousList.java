package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Experimental List implementation
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
 * The classes stored in DehydrateList MUST have a no-args constructor.
 * <p>
 * Like ArrayList mutating operations are not synchronized.
 * <p>
 * Does not allow null elements.
 * <p>
 * Implements java.util.List but some methods are not (yet) implemented mainly because they don't make much sense
 * performance-wise, like the indexed add and set methods. They mess with the memory layout. The list is meant to
 * be appended at the tail.
 */
public class ContiguousList<E> implements List<E> {

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

    private int currentValueIndex;

    private int[] valueIndices = new int[10];

    private int size;

    private final Class<?> type;

    private final List<PropertyHandler> propertyHandlers = new LinkedList<>();

    public ContiguousList(Class<E> type) {
        this.type = type;
        //have to make this recursive
        inspectType(type, new ArrayList<>());
        valueIndices[0] = 0;
    }

    /*
     * Get a list of setters and getters to execute later on to get/set the values of the object
     *
     * The order of excution is crucial, ie MUST be the same as the order in the stored data.
     *
     * The advantage of the current implementation is that the binary data is not aware of the actual
     * object graph. It only knows the 'primitive' values.
     */
    private void inspectType(Class<?> type, List<MethodHandle> childGetters) {
        try {
            final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
            Arrays.stream(type.getDeclaredFields())
                    .forEach(field -> {
                        try {
                            Class<?> fieldType = field.getType();
                            MethodHandle getter = lookup.findGetter(type, field.getName(), fieldType);
                            MethodHandle setter = lookup.findSetter(type, field.getName(), fieldType);

                            if (PropertyHandlerFactory.isKnownType(fieldType)) {
                                PropertyHandler propertyHandler = PropertyHandlerFactory.forType(fieldType, getter, setter);

                                // not empty if there has been recursion
                                if (!childGetters.isEmpty()) {
                                    childGetters.forEach(propertyHandler::addChildGetter);
                                }

                                propertyHandlers.add(propertyHandler);
                            } else {
                                // assume nested bean
                                childGetters.add(getter);
                                inspectType(fieldType, childGetters);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean addAll(Collection<? extends E> collection) {
        for (E element: collection){
            add(element);
        }
        return true;
    }

    public boolean addAll(int i, Collection<? extends E> collection) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void sort(Comparator<? super E> c) {
        throw new RuntimeException("Not implemented");
    }

    public void clear() {
        this.currentValueIndex = 0;
        this.size = 0;
    }

    @Override
    public boolean add(E element) {
        if (element == null) {
            return false;
        }
        propertyHandlers.forEach(appender -> appender.storeValue(element, this));
        size += 1;

        // keep track of where the objects are stored
        if (size > valueIndices.length) {
            this.valueIndices = Arrays.copyOf(this.valueIndices, this.valueIndices.length * 2);
        }
        valueIndices[size] = currentValueIndex;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        throw new RuntimeException("Not yet implemented");
    }

    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index <0 or >" + size);
        }
        data.position(valueIndices[index]);
        try {
            E newInstance = (E) type.getDeclaredConstructor().newInstance();
            propertyHandlers.forEach(appender -> {
                appender.setValue(newInstance, ValueReader.read(data));
            });

            return newInstance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public E set(int i, E e) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void add(int i, E e) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public E remove(int i) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public int indexOf(Object o) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public ListIterator<E> listIterator(int i) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public List<E> subList(int i, int i1) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Spliterator<E> spliterator() {
        return List.super.spliterator();
    }

    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Iterator<E> iterator() {
        return new Iter<E>();
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
    public <T> T[] toArray(T[] ts) {
        if (size > ts.length) {
            return (T[]) toArray();
        }
        for (int i = 0; i < size; i++) {
            ts[i] = (T) get(i);
        }
        return ts;
    }


    class Iter<F> implements Iterator<F> {

        private int curIndex = 0;

        @Override
        public boolean hasNext() {
            return curIndex < size;
        }

        @Override
        public F next() {
            return (F) get(curIndex++);
        }
    }

    private void store(byte[] bytes) {
        ensureCapacity(bytes.length);
        data.position(currentValueIndex); // ensures intermittent reads/writes
        data.put(bytes);
        currentValueIndex += bytes.length;
    }

    private void store(byte singlebyte) {
        ensureCapacity(1);
        data.put(singlebyte);
        currentValueIndex += 1;
    }

    void storeString(String value) {
        if (value == null) {
            store(Varint.write(0));
        } else {
            byte[] utf = value.getBytes(StandardCharsets.UTF_8);
            store(Varint.write(((long) (utf.length) << 1) + STRING_OFFSET));
            store(utf);
        }
    }

    void storeLong(Long value) {
        if (value == null) {
            store((byte) 0);
        } else {
            byte[] valueAsBytes = getValueAsBytes(value);
            store(getIntegerType(value, valueAsBytes.length));
            store(valueAsBytes);
        }
    }

    void storeInteger(Integer value) {
        if (value == null) {
            store((byte) 0);
        } else {
            byte[] valueAsBytes = getValueAsBytes(value);
            store(getIntegerType(value, valueAsBytes.length));
            store(valueAsBytes);
        }
    }

    void storeByte(Byte value) {
        if (value == null) {
            store((byte) 0);
        } else {
            byte[] valueAsBytes = getValueAsBytes(value);
            store(getIntegerType(value, valueAsBytes.length));
            store(valueAsBytes);
        }
    }

    void storeDouble(Double value) {
        if (value == null) {
            store((byte) 0);
        } else {
            store(DOUBLE_TYPE);
            store(ByteBuffer.wrap(new byte[8]).putDouble(0, value).array());
        }
    }

    void storeFloat(Float value) {
        if (value == null) {
            store((byte) 0);
        } else {
            store(FLOAT_TYPE);
            store(ByteBuffer.wrap(new byte[4]).putFloat(0, value).array());
        }
    }

    byte[] getData() {
        return Arrays.copyOfRange(data.array(), 0, currentValueIndex);
    }

    int[] getValueIndices() {
        return Arrays.copyOfRange(valueIndices, 0, size + 1);
    }

    private void ensureCapacity(int length) {
        while (currentValueIndex + length > data.capacity()) {
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
