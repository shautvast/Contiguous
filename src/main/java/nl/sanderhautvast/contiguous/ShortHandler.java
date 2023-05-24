package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

class ShortHandler extends PropertyHandler<Short> {
    public ShortHandler(MethodHandle getter, MethodHandle setter) {
        super(getter, setter);
    }

    @Override
    public void store(Short value, ContiguousList<?> list) {
        list.storeInteger(value == null ? null :  value.intValue());
    }

    @Override
    public void setValue(Object instance, Object value) {
        super.setValue(instance, ((Long) value).shortValue());
    }
}
