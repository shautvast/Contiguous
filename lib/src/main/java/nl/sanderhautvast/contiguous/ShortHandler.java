package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

class ShortHandler extends PrimitiveTypeHandler<Short> {
    public ShortHandler(MethodHandle getter, MethodHandle setter) {
        super(Short.class, getter, setter);
    }

    @Override
    public void store(Short value, ContiguousList<?> list) {
        list.storeShort(value);
    }

    @Override
    public void setValue(Object instance, Object value) {
        super.setValue(instance, ((Long) value).shortValue());
    }
}
