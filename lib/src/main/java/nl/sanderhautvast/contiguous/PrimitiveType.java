package nl.sanderhautvast.contiguous;

import java.lang.invoke.MethodHandle;

/*
 * Base class for handlers. Its responsibility is to read and write a property from the incoming object to the internal storage.
 *
 * Can be extended for types that you need to handle.
 *
 * A property handler is instantiated once per bean property and contains handles to the getter and setter methods
 * of the bean that it needs to call 'runtime' (after instantiation of the list),
 * ie. when a bean is added or retrieved from the list
 */
public abstract class PrimitiveType<T> extends Type {

    /*
     * Apology:
     * This was the simplest thing I could think of when trying to accomodate for nested types.
     *
     * What you end up with after inspection in the DehydrateList is a flat list of getters and setters
     * of properties that are in a tree-like structure (primitive properties within (nested) compound types)
     * So to read or write a property in a 'root object' (element type in list) with compound property types
     * you first have to traverse to the bean graph to the right container (bean) of the property you set/get
     * (that is what the childGetters are for)
     *
     * Ideally you'd do this only once per containing class. In the current implementation it's once per
     * property in the containing class.
     */
//    private final List<MethodHandle> childGetters = new ArrayList<>();

    public PrimitiveType(Class<?> type, MethodHandle getter, MethodHandle setter) {
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
            setter.invokeWithArguments(instance, value);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

}
