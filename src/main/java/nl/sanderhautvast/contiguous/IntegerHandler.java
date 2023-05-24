package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

class IntegerHandler extends PropertyHandler<Integer> {
    public IntegerHandler(MethodHandle getter, MethodHandle setter) {
        super(getter, setter);
    }

    /**
     * TODO improve
     * it's first extended to long (s64) and then stored with variable length.
     * With a little more code for s64, s16 and s8 specifically we can avoid the lenghtening and shortening
     */
    @Override
    public void store(Integer value, ContiguousList<?> list) {
        list.storeInteger((Integer)value);
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
