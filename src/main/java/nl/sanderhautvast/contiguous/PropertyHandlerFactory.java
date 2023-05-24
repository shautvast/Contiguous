package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/*
 * Maps the propertyvalue type to a PropertyHandler
 */
final class PropertyHandlerFactory {
    private static final Map<Class<?>, Class<? extends PropertyHandler>> APPENDERS = new HashMap<>();

    private PropertyHandlerFactory() {
    }

    static {
        APPENDERS.put(String.class, StringHandler.class);
        APPENDERS.put(byte.class, ByteHandler.class);
        APPENDERS.put(Byte.class, ByteHandler.class);
        APPENDERS.put(int.class, IntegerHandler.class);
        APPENDERS.put(Integer.class, IntegerHandler.class);
        APPENDERS.put(short.class, ShortHandler.class);
        APPENDERS.put(Short.class, ShortHandler.class);
        APPENDERS.put(long.class, LongHandler.class);
        APPENDERS.put(Long.class, LongHandler.class);
        APPENDERS.put(float.class, FloatHandler.class);
        APPENDERS.put(Float.class, FloatHandler.class);
        APPENDERS.put(double.class, DoubleHandler.class);
        APPENDERS.put(Double.class, DoubleHandler.class);
        //Date/Timestamp
        //LocalDate/time
        //BigDecimal
        //BigInteger
    }

    public static boolean isKnownType(Class<?> type) {
        return APPENDERS.containsKey(type);
    }

    public static <T> PropertyHandler forType(Class<T> type, MethodHandle getter, MethodHandle setter) {
        try {
            Class<? extends PropertyHandler> appenderClass = APPENDERS.get(type);
            if (appenderClass == null) {
                throw new IllegalStateException("No ListAppender for " + type.getName());
            }
            return appenderClass.getDeclaredConstructor(MethodHandle.class, MethodHandle.class)
                    .newInstance(getter, setter);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
