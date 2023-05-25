package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/*
 * Maps the propertyvalue type to a PropertyHandler
 */
final class PropertyHandlerFactory {
    private static final Map<Class<?>, Class<? extends PrimitiveType<?>>> STANDARD_HANDLERS = new HashMap<>();

    private PropertyHandlerFactory() {
    }

    static {
        STANDARD_HANDLERS.put(String.class, StringHandler.class);
        STANDARD_HANDLERS.put(byte.class, ByteHandler.class);
        STANDARD_HANDLERS.put(Byte.class, ByteHandler.class);
        STANDARD_HANDLERS.put(int.class, IntegerHandler.class);
        STANDARD_HANDLERS.put(Integer.class, IntegerHandler.class);
        STANDARD_HANDLERS.put(short.class, ShortHandler.class);
        STANDARD_HANDLERS.put(Short.class, ShortHandler.class);
        STANDARD_HANDLERS.put(long.class, LongHandler.class);
        STANDARD_HANDLERS.put(Long.class, LongHandler.class);
        STANDARD_HANDLERS.put(float.class, FloatHandler.class);
        STANDARD_HANDLERS.put(Float.class, FloatHandler.class);
        STANDARD_HANDLERS.put(double.class, DoubleHandler.class);
        STANDARD_HANDLERS.put(Double.class, DoubleHandler.class);
        //Date/Timestamp
        //LocalDate/time
        //BigDecimal
        //BigInteger
    }

    public static boolean isKnownType(Class<?> type) {
        return STANDARD_HANDLERS.containsKey(type);
    }

    public static <T> PrimitiveType<T> forType(Class<T> type, MethodHandle getter, MethodHandle setter) {
        try {
            Class<? extends PrimitiveType<?>> appenderClass = STANDARD_HANDLERS.get(type);
            if (appenderClass == null) {
                throw new IllegalStateException("No Handler for " + type.getName());
            }
            return (PrimitiveType<T>) appenderClass.getDeclaredConstructor(MethodHandle.class, MethodHandle.class)
                    .newInstance(getter, setter);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> PrimitiveType<T> forType(Class<T> type) {
        return forType(type, null, null);
    }
}
