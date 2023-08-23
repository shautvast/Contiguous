package com.github.shautvast.contiguous;

import com.github.shautvast.reflective.MetaMethod;

import java.lang.invoke.MethodHandle;

/**
 * Abstract basetype over handlers for 'primitives' (ie. long, but also Long,
 * String..=> built-in types) and compound types (your own).
 * Common ancestor primarily to iterator over properties of any type.
 * The respective functions are completely different though, and we need `instanceof` to check for the
 * actual type. (Rust enums!)
 */
public abstract class TypeHandler {

    protected MetaMethod getter; // both can be null, if it's for a known ('primitive') type
    protected MetaMethod setter;
    private final Class<?> type;

    /**
     * full name, prepended by all parent property names
     */
    private final String name;

    public TypeHandler(Class<?> type, String name, MetaMethod getter, MetaMethod setter) {
        this.type = type;
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    void setGetter(MetaMethod getter) {
        this.getter = getter;
    }

    public MetaMethod getGetter() {
        return getter;
    }

    public MetaMethod getSetter() {
        return setter;
    }

    void setSetter(MetaMethod setter) {
        this.setter = setter;
    }

    public Class<?> getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
