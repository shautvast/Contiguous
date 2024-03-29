package com.github.shautvast.contiguous;

import com.github.shautvast.reflective.MetaMethod;

import java.math.BigDecimal;

class BigDecimalHandler extends BuiltinTypeHandler<BigDecimal> {
    public BigDecimalHandler(String propertyName, MetaMethod getter, MetaMethod setter) {
        super(BigDecimal.class, propertyName, getter, setter);
    }

    @Override
    public void store(BigDecimal value, ContiguousList<?> list) {
        list.storeString(value.toString());
    }

    @Override
    public Object cast(Object value) {
        return new BigDecimal((String) value);
    }
}
