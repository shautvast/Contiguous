package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

class FloatHandler extends PrimitiveTypeHandler<Float> {
    public FloatHandler(MethodHandle getter, MethodHandle setter) {
        super(Float.class, getter, setter);
    }

    @Override
    public void store(Float value, ContiguousList<?> list) {
        list.storeFloat(value);
    }
}
