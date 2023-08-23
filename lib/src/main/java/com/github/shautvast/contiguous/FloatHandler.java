package com.github.shautvast.contiguous;

import com.github.shautvast.reflective.MetaMethod;

class FloatHandler extends BuiltinTypeHandler<Float> {
    public FloatHandler(String propertyName, MetaMethod getter, MetaMethod setter) {
        super(Float.class, propertyName, getter, setter);
    }

    @Override
    public void store(Float value, ContiguousList<?> list) {
        list.storeFloat(value);
    }
}
