/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.function;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.Expression;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.hgs.HGSMap;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Used to call a java function from the template code.
 * Uses reflection to invoke the methods.
 * All public methods which are declared in the given class are available via a
 * {@link HGSMap} instance obtained by calling {@link JavaClass#createMap(Object)}.
 * See {@link de.neemann.digital.hdl.vhdl.lib.VHDLTemplate} as an example.
 *
 * @param <T> the type of the instance
 */
public final class JavaClass<T> {
    private final HashMap<String, MyMethod<T>> methods;

    /**
     * Creates a new instance
     *
     * @param clazz the class
     */
    public JavaClass(Class<T> clazz) {
        methods = new HashMap<>();
        for (Method m : clazz.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod)) {
                final String name = m.getName();
                if (methods.containsKey(name))
                    throw new RuntimeException("Method overloading ("
                            + name + ") is not supported! Try to use the ellipsis (...) instead.");
                methods.put(name, new MyMethod<>(m));
            }
        }
    }

    /**
     * Creates the method map.
     *
     * @param instance the instance to call
     * @return the method map
     */
    public HGSMap createMap(T instance) {
        return new MethodMap<>(this, instance);
    }

    private static final class MyMethod<T> {
        private final Method method;
        private final boolean isStatic;
        private final boolean addContext;
        private final int argCount;
        private final int javaArgCount;
        private final boolean isVarArgs;
        private final Class<?> compType;

        private MyMethod(Method method) {
            this.method = method;
            this.isStatic = Modifier.isStatic(method.getModifiers());

            Class<?>[] argTypes = method.getParameterTypes();
            javaArgCount = argTypes.length;
            addContext = (argTypes.length > 0 && argTypes[0].isAssignableFrom(Context.class));

            isVarArgs = method.isVarArgs();
            if (isVarArgs) {
                argCount = -1;
                compType = argTypes[argTypes.length - 1].getComponentType();
            } else {
                if (addContext)
                    argCount = argTypes.length - 1;
                else
                    argCount = argTypes.length;
                compType = null;
            }
        }

        private Object call(T instance, Context c, ArrayList<Expression> args) throws HGSEvalException {
            if (instance == null && !isStatic)
                throw new HGSEvalException("Function " + method.getName() + " is not static!");

            if (argCount >= 0 && argCount != args.size())
                throw new HGSEvalException("Wrong number of arguments! expected: "
                        + argCount + ", but found:" + args.size());

            Object[] a = new Object[javaArgCount];
            int i = 0;
            if (addContext) {
                a[0] = c;
                i++;
            }

            if (!isVarArgs) {
                for (Expression exp : args) {
                    a[i] = exp.value(c);
                    i++;
                }
            } else {
                // ellipsis
                try {
                    // the fixed args
                    int fixed = javaArgCount - i - 1;
                    for (int n = 0; n < fixed; n++) {
                        a[i] = args.get(n).value(c);
                        i++;
                    }
                    // put the var args to an array
                    final int numVarArgs = args.size() - fixed;
                    Object varArgs = Array.newInstance(compType, numVarArgs);
                    for (int n = fixed; n < args.size(); n++)
                        Array.set(varArgs, n - fixed, args.get(n).value(c));

                    // and pass the array
                    a[i] = varArgs;
                } catch (RuntimeException e) {
                    throw new HGSEvalException("type error assigning value to var array in "
                            + method.getName() + ". Type "
                            + compType.getSimpleName() + " is required.");
                }
            }

            try {
                return method.invoke(instance, a);
            } catch (RuntimeException | IllegalAccessException | InvocationTargetException e) {
                throw new HGSEvalException("Error invoking the java method " + method.getName() + "!", e);
            }
        }
    }

    private static final class MethodMap<T> implements HGSMap {
        private final JavaClass<T> javaClass;
        private final T instance;

        private MethodMap(JavaClass<T> javaClass, T instance) {
            this.javaClass = javaClass;
            this.instance = instance;
        }

        @Override
        public Object hgsMapGet(String key) {
            MyMethod<T> m = javaClass.methods.get(key);
            if (m == null) return null;
            return new MethodCall<>(m, instance);
        }
    }

    private static final class MethodCall<T> extends InnerFunction {
        private final MyMethod<T> m;
        private final T instance;

        private MethodCall(MyMethod<T> m, T instance) {
            super(m.argCount);
            this.m = m;
            this.instance = instance;
        }

        @Override
        public Object call(Context c, ArrayList<Expression> args) throws HGSEvalException {
            return m.call(instance, c, args);
        }
    }

}
