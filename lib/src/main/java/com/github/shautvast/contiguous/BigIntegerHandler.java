package com.github.shautvast.contiguous;

import java.lang.invoke.MethodHandle;
import java.math.BigInteger;

class BigIntegerHandler extends BuiltinTypeHandler<BigInteger> {
    public BigIntegerHandler(String propertyName, MethodHandle getter, MethodHandle setter) {
        super(BigInteger.class, propertyName, getter, setter);
    }

    @Override
    public void store(BigInteger value, ContiguousList<?> list) {
        list.storeString(value.toString());
    }

    @Override
    public Object cast(Object value) {
        return new BigInteger((String) value);
    }
}
