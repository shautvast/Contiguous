package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/*
 * Maps the propertyvalue type to a PropertyHandler
 */
final class PropertyHandlerFactory {
    private static final Map<Class<?>, Class<? extends PrimitiveTypeHandler<?>>> TYPE_HANDLERS = new HashMap<>();

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

    public static boolean isKnownType(Class<?> type) {
        return TYPE_HANDLERS.containsKey(type);
    }

    public static <T> PrimitiveTypeHandler<T> forType(Class<T> type, MethodHandle getter, MethodHandle setter) {
        try {
            Class<? extends PrimitiveTypeHandler<?>> appenderClass = TYPE_HANDLERS.get(type);
            if (appenderClass == null) {
                throw new IllegalStateException("No Handler for " + type.getName());
            }
            return (PrimitiveTypeHandler<T>) appenderClass.getDeclaredConstructor(MethodHandle.class, MethodHandle.class)
                    .newInstance(getter, setter);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> PrimitiveTypeHandler<T> forType(Class<T> type) {
        return forType(type, null, null);
    }

    /**
     * register a new TypeHandler that cannot be derived from bean properties
     */
    public static void register(Class<?> type, Class<? extends PrimitiveTypeHandler<?>> typehandler) {
        TYPE_HANDLERS.put(type, typehandler);
    }
}
