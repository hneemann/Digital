/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.function;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.Expression;
import de.neemann.digital.hdl.hgs.HGSEvalException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * Used to call a java function from the template code.
 * Uses reflection to invoke the method;
 */
public final class JavaMethod extends InnerFunction {
    private final Object instance;
    private final Method method;
    private final Class<?>[] argTypes;
    private final boolean addContext;

    /**
     * Creates a function for a static method.
     * If the first argument of the method is of type {@link Context} the
     * context is passed in.
     *
     * @param c    the class
     * @param name the name of the method
     * @return the function
     */
    public static JavaMethod create(Class c, String name) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name))
                if (Modifier.isStatic(m.getModifiers())) {
                    Class<?>[] argTypes = m.getParameterTypes();
                    return create(null, m, argTypes);
                }
        }
        throw new RuntimeException("method '" + name + "' not found in " + c.getName() + "!");
    }

    /**
     * Creates a function for a non static method.
     * If the first argument of the method is of type {@link Context} the
     * context is passed in.
     *
     * @param inst the instance to use for the call
     * @param name the name of the method
     * @return the function
     */
    public static JavaMethod create(Object inst, String name) {
        final Class<?> c = inst.getClass();
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name))
                if (!Modifier.isStatic(m.getModifiers())) {
                    Class<?>[] argTypes = m.getParameterTypes();
                    return create(inst, m, argTypes);
                }
        }
        throw new RuntimeException("method '" + name + "' not found in " + c.getName() + "!");
    }

    private static JavaMethod create(Object inst, Method m, Class<?>[] argTypes) {
        if (argTypes.length > 0 && argTypes[0].isAssignableFrom(Context.class))
            return new JavaMethod(argTypes.length - 1, inst, m, argTypes, true);
        else
            return new JavaMethod(argTypes.length, inst, m, argTypes, false);
    }

    private JavaMethod(int argCount, Object instance, Method method, Class<?>[] argTypes, boolean addContext) {
        super(argCount);
        this.instance = instance;
        this.method = method;
        this.argTypes = argTypes;
        this.addContext = addContext;
    }

    @Override
    public Object call(Context c, ArrayList<Expression> args) throws HGSEvalException {
        if (getArgCount() != args.size())
            throw new HGSEvalException("wrong number of arguments! expected: " + getArgCount() + ", but found:" + args.size());

        Object[] a;
        int i = 0;
        if (addContext) {
            a = new Object[args.size() + 1];
            a[0] = c;
            i++;
        } else
            a = new Object[args.size()];

        for (Expression exp : args) {
            a[i] = exp.value(c);
            i++;
        }

        return f(a);
    }


    private Object f(Object... args) throws HGSEvalException {
        for (int i = 0; i < args.length; i++) {
            if (!argTypes[i].isAssignableFrom(args[i].getClass()))
                throw new HGSEvalException("Argument " + i + " has wrong type! Expected: "
                        + argTypes[i].getName()
                        + ", found "
                        + args[i].getClass().getName());
        }

        try {
            return method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new HGSEvalException("Error invoking the java method " + method.getName() + "!", e);
        }
    }
}
