package com.github.shautvast.contiguous.asm;

import com.github.shautvast.contiguous.ValueReader;
import com.github.shautvast.contiguous.Varint;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class AsmContiguousList<E> {

    private static final byte[] DOUBLE_TYPE = {7};
    private static final byte[] FLOAT_TYPE = {10}; // not in line with SQLite anymore
    private static final int STRING_OFFSET = 13;
    private static final int BYTES_OFFSET = 12; // blob TODO decide if include
    public static final int MAX_24BITS = 8388607;
    public static final long MAX_48BITS = 140737488355327L;

    /*
     * storage for dehydrated objects
     */
    private ByteBuffer data = ByteBuffer.allocate(4096);//TODO create constructor with capacity

    private int bufferPosition;

    private final ArrayList<Integer> elementIndices = new ArrayList<>();

    private int size;

    private final AsmTypeHandler<?> rootHandler;
//    private static final Map<Class<?>, TypeHandler> TYPE_HANDLERS = new HashMap<>();

    public AsmContiguousList(Class<E> elementType) {
        this.rootHandler = new StringHandler();
        elementIndices.add(0); // index of first element
        bufferPosition = 0;
    }

    public boolean add(E element) {
        if (element == null) {
            return false;
        }
        storePropertyData(element, rootHandler);
        extend();
        return true;
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        checkbounds(index);
        data.position(elementIndices.get(index));
        Object read = ValueReader.read(data);
        return (E) rootHandler.cast(read);
        // create a new instance of the list element type
    }

    public String getAsJson(int index) {
        checkbounds(index);
        data.position(elementIndices.get(index));
        if (rootHandler instanceof BuiltinHandler<?>) {
            return getValue(rootHandler);
        }
        return null;
    }

    private void checkbounds(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index <0 or >" + size);
        }
    }

    private String getValue(AsmTypeHandler<?> handler) {
        String value = String.valueOf(ValueReader.read(data));
        if (handler instanceof BuiltinHandler<?>) {
            return quote(value);
        }
        return value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void storePropertyData(Object element, AsmTypeHandler<?> typeHandler) {
        if (typeHandler instanceof BuiltinHandler<?>) {
            typeHandler.storePropertyValue(element);
        }
    }

    private String quote(String out) {
        out = "\"" + out + "\"";
        return out;
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

    private void ensureFree(int length) {
        while (bufferPosition + length > data.capacity()) {
            ByteBuffer bytes = this.data;
            this.data = ByteBuffer.allocate(this.data.capacity() * 2);
            this.data.put(bytes);
        }
    }

    void extend() {
        size += 1;
        // keep track of index of element in data
        elementIndices.add(bufferPosition);
    }

    class StringHandler extends BuiltinHandler<String> {
        @Override
        protected String getValue(Object instance) {
            return instance.toString();
        }

        public void store(String value){
            storeString(value);
        }
    }
}

