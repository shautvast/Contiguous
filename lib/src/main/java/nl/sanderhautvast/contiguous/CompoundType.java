package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;

class CompoundType extends Type {
    private final Map<String, Type> properties = new LinkedHashMap<>();

    private MethodHandle getter;
    private MethodHandle setter;


    CompoundType(Class<?> type) {
        super(type, null,null);
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

    Collection<Type> getProperties() {
        return properties.values();
    }

    void addHandler(String propertyName, PrimitiveType<?> primitiveType) {
        properties.put(propertyName, primitiveType);
    }

    void addChild(Field property, CompoundType childCompoundType) {
        this.properties.put(property.getName(), childCompoundType);
    }


}
