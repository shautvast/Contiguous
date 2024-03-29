package com.github.shautvast.contiguous;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

class CompoundTypeHandler extends TypeHandler {
    private final Map<String, TypeHandler> properties = new LinkedHashMap<>();

    CompoundTypeHandler(Class<?> type) {
        super(type, null,null, null);
    }

    CompoundTypeHandler(Class<?> type, String propertyName) {
        super(type, propertyName, null,null);
    }


    Collection<TypeHandler> getProperties() {
        return properties.values();
    }

    void addHandler(String propertyName, BuiltinTypeHandler<?> primitiveType) {
        properties.put(propertyName, primitiveType);
    }

    void addChild(Field property, CompoundTypeHandler childCompoundType) {
        this.properties.put(property.getName(), childCompoundType);
    }
}
