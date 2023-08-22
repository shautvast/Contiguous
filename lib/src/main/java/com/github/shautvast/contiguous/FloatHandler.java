package com.github.shautvast.contiguous;

import java.lang.invoke.MethodHandle;

class FloatHandler extends BuiltinTypeHandler<Float> {
    public FloatHandler(String propertyName, MethodHandle getter, MethodHandle setter) {
        super(Float.class, propertyName, getter, setter);
    }

    @Override
    public void store(Float value, ContiguousList<?> list) {
        list.storeFloat(value);
    }
}
