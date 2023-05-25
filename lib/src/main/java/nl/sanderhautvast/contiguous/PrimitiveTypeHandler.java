package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

/**
 * Base class for handlers. Its responsibility is to read and write a property from the incoming object to the internal storage.
 * <p>
 * Can be extended for types that you need to handle.
 * <p>
 * A property handler is instantiated once per bean property and contains handles to the getter and setter methods
 * of the bean that it needs to call 'runtime' (after instantiation of the list),
 * ie. when a bean is added or retrieved from the list
 */
public abstract class PrimitiveTypeHandler<T> extends TypeHandler {
    public PrimitiveTypeHandler(Class<?> type, MethodHandle getter, MethodHandle setter) {
        super(type, getter, setter);
    }

    /**
     * Subclasses call the appropriate store method on the ContiguousList
     *
     * @param value the value to store
     * @param list  where to store the value
     */
    public abstract void store(T value, ContiguousList<?> list);

    void storePropertyValue(Object instance, ContiguousList<?> typedList) {
        T propertyValue = getValue(instance);
        store(propertyValue, typedList);
    }

    private T getValue(Object propertyValue) {
        // I don't trust this
        if (getter == null) {
            return (T) propertyValue;
        }

        try {
            return (T) getter.invoke(propertyValue);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * This is used when get() is called on the list.
     * As this will create a new instance of the type, it's property values need to be set.
     * <p>
     * Can be overridden to do transformations on the value after it has been retrieved, but make sure
     * to call super.setValue() or the value won't be set.
     *
     * @param instance the created type
     * @param value    the value that has been read from ContiguousList storage
     */
    public void setValue(Object instance, Object value) {
        try {
            setter.invokeWithArguments(instance, transform(value));
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Certain types can easily be stored as another known type, for instance
     * a BigDecimal can be stored as a String.
     * <p>
     * The {@link PrimitiveTypeHandler} for BigDecimal would in that case be responsible for turning the String
     * into a BigDecimal. It can do that by overriding this method
     *
     * @param value raw value to transform to the desired output type
     * @return the transformed object
     */
    public Object transform(Object value) {
        return value;
    }
}
