package com.github.shautvast.contiguous;

import com.github.shautvast.reflective.MetaMethod;

class StringHandler extends BuiltinTypeHandler<String> {
    public StringHandler(String propertyName, MetaMethod getter, MetaMethod setter) {
        super(String.class, propertyName, getter, setter);
    }

    @Override
    public void store(String value, ContiguousList<?> list) {
        list.storeString(value);
    }
}
