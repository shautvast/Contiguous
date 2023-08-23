package com.github.shautvast.contiguous;

import com.github.shautvast.reflective.MetaMethod;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/*
 * Maps the propertyvalue type to a PropertyHandler
 */
final class PropertyHandlerFactory {
    private static final Map<Class<?>, Class<? extends BuiltinTypeHandler<?>>> BUILTIN = new HashMap<>();

    private PropertyHandlerFactory() {
    }

    static {
        register(String.class, StringHandler.class);
        register(byte.class, ByteHandler.class);
        register(Byte.class, ByteHandler.class);
        register(int.class, IntegerHandler.class);
        register(Integer.class, IntegerHandler.class);
        register(short.class, ShortHandler.class);
        register(Short.class, ShortHandler.class);
        register(long.class, LongHandler.class);
        register(Long.class, LongHandler.class);
        register(float.class, FloatHandler.class);
        register(Float.class, FloatHandler.class);
        register(double.class, DoubleHandler.class);
        register(Double.class, DoubleHandler.class);
        register(BigDecimal.class, StringHandler.class);
        register(BigInteger.class, BigIntegerHandler.class);
        register(BigDecimal.class, BigDecimalHandler.class);
        //Date/Timestamp
        //LocalDate/time

    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<TypeHandler> forType(Class<T> type, String name, MetaMethod getter, MetaMethod setter) {
        return Optional.ofNullable(BUILTIN.get(type)).map(appenderClass -> {
            try {
                return (BuiltinTypeHandler<T>) appenderClass.getDeclaredConstructor(String.class, MetaMethod.class, MetaMethod.class)
                        .newInstance(name, getter, setter);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public static <T> Optional<TypeHandler> forType(Class<T> type) {
        return forType(type, null, null, null);
    }

    /**
     * register a new TypeHandler that cannot be derived from bean properties
     */
    public static void register(Class<?> type, Class<? extends BuiltinTypeHandler<?>> typehandler) {
        BUILTIN.put(type, typehandler);
    }
}
