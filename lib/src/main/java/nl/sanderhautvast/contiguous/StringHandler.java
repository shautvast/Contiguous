package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

class StringHandler extends PrimitiveType<String> {
    public StringHandler(MethodHandle getter, MethodHandle setter) {
        super(String.class, getter, setter);
    }

    @Override
    public void store(String value, ContiguousList<?> list) {
        list.storeString(value);
    }
}
