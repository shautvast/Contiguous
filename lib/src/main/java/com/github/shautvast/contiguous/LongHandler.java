package com.github.shautvast.contiguous;

import com.github.shautvast.reflective.MetaMethod;

import java.lang.invoke.MethodHandle;

class LongHandler extends BuiltinTypeHandler<Long> {
    public LongHandler(String propertyName, MetaMethod getter, MetaMethod setter) {
        super(Long.class, propertyName, getter, setter);
    }

    @Override
    public void store(Long value, ContiguousList<?> list) {
        list.storeLong(value);
    }
}
