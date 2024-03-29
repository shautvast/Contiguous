package com.github.shautvast.contiguous;

import com.github.shautvast.reflective.MetaMethod;

class IntegerHandler extends BuiltinTypeHandler<Integer> {
    public IntegerHandler(String propertyName, MetaMethod getter, MetaMethod setter) {
        super(Integer.class, propertyName, getter, setter);
    }

    @Override
    public void store(Integer value, ContiguousList<?> list) {
        list.storeInteger(value);
    }

    /*
     * Every integer number is considered a (variable length) long in the storage
     * This method makes sure it's cast back to the required type (same for byte and short)
     */
    @Override
    public void setValue(Object instance, Object value) {
        super.setValue(instance, ((Long) value).intValue());
    }

    @Override
    public Object cast(Object value) {
        // could be Long (raw value)
        // or Integer when it's a property with known type
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        return value;
    }
}
