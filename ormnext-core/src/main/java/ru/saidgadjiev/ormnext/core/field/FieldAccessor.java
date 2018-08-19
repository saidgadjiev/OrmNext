package ru.saidgadjiev.ormnext.core.field;

import ru.saidgadjiev.ormnext.core.exception.FieldAccessException;
import ru.saidgadjiev.logger.Log;
import ru.saidgadjiev.logger.LoggerFactory;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * This class may be used for access to field.
 * It access to field by lambda expression.
 * Getter will be mapped to {@link Function}, setter will be mapped to {@link BiConsumer}.
 *
 * @author Said Gadjiev
 */
public class FieldAccessor {

    /**
     * Logger.
     */
    private static final Log LOG = LoggerFactory.getLogger(FieldAccessor.class);

    /**
     * Setter.
     */
    private BiConsumer<Object, Object> setter;

    /**
     * Getter.
     */
    private Function<Object, Object> getter;

    /**
     * Create a new instance.
     *
     * @param field target field
     */
    public FieldAccessor(Field field) {
        Constructor<MethodHandles.Lookup> lookupConstructor = resolveLookupConstructor();

        if (!resolveGetter(field, lookupConstructor)) {
            resolveFieldGetter(field, lookupConstructor);
        }
        if (!resoveSetter(field, lookupConstructor)) {
            resolveFieldSetter(field, lookupConstructor);
        }
    }

    /**
     * Find {@link java.lang.invoke.MethodHandles.Lookup} constructor.
     *
     * @return resolved constructor
     */
    private Constructor<MethodHandles.Lookup> resolveLookupConstructor() {
        try {
            return MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Assign value to field.
     *
     * @param object target object
     * @param value  target value
     */
    public void assign(Object object, Object value) {
        setter.accept(object, value);
    }

    /**
     * Access to field and return value.
     *
     * @param object target object
     * @return field value
     */
    public Object access(Object object) {
        return getter.apply(object);
    }

    /**
     * Resolve field getter. Try find getter method.
     *
     * @param lookupConstructor lookup constructor
     * @param field             target field
     * @return true if getter method found
     */
    private boolean resolveGetter(Field field, Constructor<MethodHandles.Lookup> lookupConstructor) {
        String getterName;

        if (field.isAnnotationPresent(Getter.class)) {
            getterName = field.getAnnotation(Getter.class).name();
        } else {
            getterName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        }
        try {
            Method getter = field.getDeclaringClass().getDeclaredMethod(getterName);

            lookupConstructor.setAccessible(true);
            MethodHandles.Lookup lookup = lookupConstructor.newInstance(field.getDeclaringClass());
            lookupConstructor.setAccessible(false);
            MethodHandle getterHandle = lookup.unreflect(getter);

            this.getter = makeGetter(lookup, getterHandle);

            LOG.debug(
                    "Resolve getter %s for %s",
                    getterName,
                    field.toString()
            );
            return true;
        } catch (Throwable ex) {
            LOG.error(
                    "Getter %s for %s not found",
                    getterName,
                    field.toString()
            );
            return false;
        }
    }

    /**
     * Resolve field setter. Try find setter method.
     *
     * @param field             target field
     * @param lookupConstructor lookup constructor
     * @return true if setter method found
     */
    private boolean resoveSetter(Field field, Constructor<MethodHandles.Lookup> lookupConstructor) {
        String setterName;

        if (field.isAnnotationPresent(Setter.class)) {
            setterName = field.getAnnotation(Setter.class).name();
        } else {
            setterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        }
        try {
            Method setter = field.getDeclaringClass().getDeclaredMethod(setterName, field.getType());
            lookupConstructor.setAccessible(true);
            MethodHandles.Lookup lookup = lookupConstructor.newInstance(field.getDeclaringClass());
            lookupConstructor.setAccessible(false);

            setter.setAccessible(true);
            MethodHandle setterHandle = lookup.unreflect(setter);

            this.setter = makeSetter(lookup, setterHandle);

            LOG.debug(
                    "Resolve setter %s for %s",
                    setterName,
                    field.toString()
            );
            return true;
        } catch (Throwable ex) {
            LOG.error("Setter %s for %s not found",
                    setterName,
                    field.toString()
            );
            return false;
        }
    }

    /**
     * Resolve field setter. Try make setter by {@link Field#set(Object, Object)}.
     *
     * @param lookupConstructor lookup constructor
     * @param field             target field
     */
    private void resolveFieldSetter(Field field, Constructor<MethodHandles.Lookup> lookupConstructor) {
        try {
            lookupConstructor.setAccessible(true);
            MethodHandles.Lookup lookup = lookupConstructor.newInstance(field.getDeclaringClass());
            lookupConstructor.setAccessible(false);
            MethodHandle fieldSetterHandle = lookup.unreflectSetter(field);

            this.setter = MethodHandleProxies
                    .asInterfaceInstance(BiConsumer.class, fieldSetterHandle);
        } catch (Throwable ex) {
            LOG.error(ex.getMessage(), ex);
            throw new FieldAccessException(ex);
        }
    }

    /**
     * Resolve field getter. Try make getter by {@link Field#get(Object)}.
     *
     * @param lookupConstructor lookup constructor
     * @param field             target field
     */
    private void resolveFieldGetter(Field field, Constructor<MethodHandles.Lookup> lookupConstructor) {
        try {
            lookupConstructor.setAccessible(true);
            MethodHandles.Lookup lookup = lookupConstructor.newInstance(field.getDeclaringClass());
            lookupConstructor.setAccessible(false);
            MethodHandle fieldGetterHandle = lookup.unreflectGetter(field);

            this.getter = MethodHandleProxies
                    .asInterfaceInstance(Function.class, fieldGetterHandle);
        } catch (Throwable ex) {
            LOG.error(ex.getMessage(), ex);
            throw new FieldAccessException(ex);
        }
    }

    /**
     * Map method handle to {@link Function}.
     *
     * @param lookup target lookup
     * @param handle target handle
     * @return function
     * @throws Throwable any exceptions
     */
    private static Function<Object, Object> makeGetter(MethodHandles.Lookup lookup, MethodHandle handle)
            throws Throwable {
        return (Function<Object, Object>) LambdaMetafactory.metafactory(
                lookup,
                "apply",
                MethodType.methodType(Function.class),
                MethodType.methodType(Object.class, Object.class),
                handle,
                handle.type()
        ).getTarget().invokeExact();
    }

    /**
     * Map method handle to {@link BiConsumer}.
     *
     * @param lookup target lookup
     * @param handle target handle
     * @return function
     * @throws Throwable any exceptions
     */
    private static BiConsumer<Object, Object> makeSetter(MethodHandles.Lookup lookup, MethodHandle handle)
            throws Throwable {
        return (BiConsumer<Object, Object>) LambdaMetafactory.metafactory(
                lookup,
                "accept",
                MethodType.methodType(BiConsumer.class),
                MethodType.methodType(void.class, Object.class, Object.class),
                handle,
                handle.type()
        ).getTarget().invokeExact();
    }
}
