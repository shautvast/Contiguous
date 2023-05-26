package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;
import java.math.BigInteger;

class BigIntegerHandler extends BuiltinTypeHandler<BigInteger> {
    public BigIntegerHandler(MethodHandle getter, MethodHandle setter) {
        super(BigInteger.class, getter, setter);
    }

    @Override
    public void store(BigInteger value, ContiguousList<?> list) {
        list.storeString(value.toString());
    }

    @Override
    public Object transform(Object value) {
        return new BigInteger((String) value);
    }
}
