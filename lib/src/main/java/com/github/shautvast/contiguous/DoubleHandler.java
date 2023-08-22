package com.github.shautvast.contiguous;

import java.lang.invoke.MethodHandle;

/**
 * Stores a double value.
 */
class DoubleHandler extends BuiltinTypeHandler<Double> {
    public DoubleHandler(String propertyName, MethodHandle getter, MethodHandle setter) {
        super(Double.class, propertyName, getter, setter);
    }

    @Override
    public void store(Double value, ContiguousList<?> list) {
        list.storeDouble(value);
    }
}
