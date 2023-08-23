package com.github.shautvast.contiguous;

import com.github.shautvast.reflective.MetaMethod;

/**
 * Stores a byte value.
 */
class ByteHandler extends BuiltinTypeHandler<Byte> {

    public ByteHandler(String propertyName, MetaMethod getter, MetaMethod setter) {
        super(Byte.class, propertyName, getter, setter);
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
    public Object cast(Object value) {
        if (value instanceof Long) {
            return ((Long) value).byteValue();
        }
        return value;
    }
}
