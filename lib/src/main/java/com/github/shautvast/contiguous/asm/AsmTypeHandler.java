package com.github.shautvast.contiguous.asm;

public abstract class AsmTypeHandler<T> {
    public Object cast(Object value) {
        return value;
    }

    void storePropertyValue(Object instance) {
        T propertyValue = getValue(instance);
        store(propertyValue);
    }

    protected abstract T getValue(Object instance);

    public abstract void store(T value);
}
