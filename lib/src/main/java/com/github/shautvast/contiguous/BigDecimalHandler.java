package com.github.shautvast.contiguous;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;

class BigDecimalHandler extends BuiltinTypeHandler<BigDecimal> {
    public BigDecimalHandler(String propertyName, MethodHandle getter, MethodHandle setter) {
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
