package com.github.shautvast.contiguous;

import com.github.shautvast.reflective.MetaMethod;

/**
 * Stores a double value.
 */
class DoubleHandler extends BuiltinTypeHandler<Double> {
    public DoubleHandler(String propertyName, MetaMethod getter, MetaMethod setter) {
        super(Double.class, propertyName, getter, setter);
    }

    @Override
    public void store(Double value, ContiguousList<?> list) {
        list.storeDouble(value);
    }
}
