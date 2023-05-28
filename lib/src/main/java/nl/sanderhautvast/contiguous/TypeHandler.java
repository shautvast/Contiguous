package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

/**
 * Abstract basertype over handlers for 'primitives' (ie. long, but also Long,
 * String..=> built-in types) and compound types (your own).
 *
 * Common ancestor primarily to iterator over properties of any type.
 * The respective functions are completely different though, and we need `instanceof` to check for the
 * actual type. (Rust enums!)
 */
public abstract class TypeHandler {

    protected MethodHandle getter; // both can be null, if it's for a known ('primitive') type
    protected MethodHandle setter;
    private final Class<?> type;

    public TypeHandler(Class<?> type, MethodHandle getter, MethodHandle setter) {
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    void setGetter(MethodHandle getter) {
        this.getter = getter;
    }

    public MethodHandle getGetter() {
        return getter;
    }

    public MethodHandle getSetter() {
        return setter;
    }

    void setSetter(MethodHandle setter) {
        this.setter = setter;
    }

    public Class<?> getType() {
        return type;
    }

}
