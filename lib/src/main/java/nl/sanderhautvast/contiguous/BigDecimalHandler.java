package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;

class BigDecimalHandler extends PrimitiveTypeHandler<BigDecimal> {
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
