package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

/*
 * ok, sorry
 *
 * I needed to abstract over handlers for 'primitives' (ie. long, but also Long, String..=> built-in types) and compound types (your own)
 * So this is the common ancestor. The respective functions are completely different.
 */
public abstract class Type {

    protected MethodHandle getter; // both can be null, if it's for a known ('primitive') type
    protected MethodHandle setter;
    protected Class<?> type;

    public Type(Class<?> type, MethodHandle getter, MethodHandle setter) {
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }
}
