package nl.sanderhautvast.contiguous;

import java.lang.reflect.Field;
import java.util.*;

class CompoundTypeHandler extends TypeHandler {
    private final Map<String, TypeHandler> properties = new LinkedHashMap<>();



    CompoundTypeHandler(Class<?> type) {
        super(type, null,null);
    }


    Collection<TypeHandler> getProperties() {
        return properties.values();
    }

    void addHandler(String propertyName, PrimitiveTypeHandler<?> primitiveType) {
        properties.put(propertyName, primitiveType);
    }

    void addChild(Field property, CompoundTypeHandler childCompoundType) {
        this.properties.put(property.getName(), childCompoundType);
    }


}