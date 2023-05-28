package nl.sanderhautvast.contiguous;

import java.lang.reflect.Field;
import java.util.*;

class CompoundTypeHandler extends TypeHandler {
    private final Map<String, TypeHandler> properties = new LinkedHashMap<>();



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
