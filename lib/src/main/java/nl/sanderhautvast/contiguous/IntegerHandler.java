package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

class IntegerHandler extends PrimitiveTypeHandler<Integer> {
    public IntegerHandler(MethodHandle getter, MethodHandle setter) {
        super(Integer.class, getter, setter);
    }

    @Override
    public void store(Integer value, ContiguousList<?> list) {
        list.storeInteger(value);
    }

    /*
     * Every integer number is considered a (variable length) long in the storage
     * This method makes sure it's cast back to the required type (same for byte and short)
     */
    @Override
    public void setValue(Object instance, Object value) {
        super.setValue(instance, ((Long) value).intValue());
    }
}
