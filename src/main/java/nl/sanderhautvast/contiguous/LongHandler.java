package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

class LongHandler extends PropertyHandler<Long> {
    public LongHandler(MethodHandle getter, MethodHandle setter) {
        super(getter, setter);
    }

    @Override
    public void store(Long value, ContiguousList<?> list) {
        list.storeLong(value);
    }
}
