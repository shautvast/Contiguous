package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;
import java.math.BigInteger;

class BigDecimalHandler extends PrimitiveType<BigDecimal> {
    public BigDecimalHandler(MethodHandle getter, MethodHandle setter) {
        super(BigDecimal.class, getter, setter);
    }

    @Override
    public void store(BigDecimal value, ContiguousList<?> list) {
        list.storeString(value.toString());
    }

    @Override
    public Object transform(Object value) {
        return new BigDecimal((String) value);
    }
}
