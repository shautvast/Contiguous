package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

/**
 * Stores a byte value.
 */
class ByteHandler extends BuiltinTypeHandler<Byte> {

    public ByteHandler(MethodHandle getter, MethodHandle setter) {
        super(Byte.class, getter, setter);
    }

    @Override
    public void store(Byte value, ContiguousList<?> list) {
        list.storeByte(value);
    }

    @Override
    public void setValue(Object instance, Object value) {
        super.setValue(instance, ((Long) value).byteValue());
    }

    @Override
    public Object transform(Object value) {
        if (value instanceof Long) {
            return ((Long) value).byteValue();
        }
        return value;
    }
}
