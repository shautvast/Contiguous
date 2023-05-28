package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

class LongHandler extends BuiltinTypeHandler<Long> {
    public LongHandler(String propertyName, MethodHandle getter, MethodHandle setter) {
        super(Long.class, propertyName, getter, setter);
    }

    @Override
    public void store(Long value, ContiguousList<?> list) {
        list.storeLong(value);
    }
}
