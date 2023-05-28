package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

class StringHandler extends BuiltinTypeHandler<String> {
    public StringHandler(String propertyName, MethodHandle getter, MethodHandle setter) {
        super(String.class, propertyName, getter, setter);
    }

    @Override
    public void store(String value, ContiguousList<?> list) {
        list.storeString(value);
    }
}
