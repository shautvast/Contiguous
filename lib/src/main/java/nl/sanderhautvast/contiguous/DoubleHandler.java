package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

/**
 * Stores a double value.
 */
class DoubleHandler extends BuiltinTypeHandler<Double> {
    public DoubleHandler(MethodHandle getter, MethodHandle setter) {
        super(Double.class, getter, setter);
    }

    @Override
    public void store(Double value, ContiguousList<?> list) {
        list.storeDouble(value);
    }
}
