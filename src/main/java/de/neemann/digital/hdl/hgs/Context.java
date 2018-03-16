/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import de.neemann.digital.hdl.hgs.function.Func;
import de.neemann.digital.hdl.hgs.function.FuncAdapter;
import de.neemann.digital.hdl.hgs.function.Function;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The evaluation context
 */
public class Context {
    private final Context parent;
    private final StringBuilder code;
    private HashMap<String, Object> map;
    private boolean functionContext = false;

    /**
     * Creates a new context
     */
    public Context() {
        this(null, true);
    }

    /**
     * Creates a new context
     *
     * @param parent the parent context
     */
    public Context(Context parent) {
        this(parent, true);
    }

    /**
     * Creates a new context
     *
     * @param parent      the parent context
     * @param enablePrint enables the print, if false, the printing goes to the parent of this context
     */
    public Context(Context parent, boolean enablePrint) {
        this.parent = parent;
        if (enablePrint)
            this.code = new StringBuilder();
        else
            this.code = null;
        map = new HashMap<>();

        // some functions which are always present
        addFunc("print", new FunctionPrint());
        addFunc("printf", new FunctionPrintf());
        addFunc("format", new FunctionFormat());
        addFunc("isPresent", new FunctionIsPresent());
        addFunc("panic", new FunctionPanic());
        addFunc("sizeOf", new Func(1, args -> Value.toArray(args[0]).hgsArraySize()));
        addFunc("newMap", new Func(0, args -> new HashMap()));
        addFunc("newList", new Func(0, args -> new ArrayList()));
    }

    /**
     * Returns true if this context contains a mapping for the specified key.
     *
     * @param name the key
     * @return true if value is present
     */
    public boolean contains(String name) {
        if (map.containsKey(name))
            return true;
        else {
            if (parent != null)
                return parent.contains(name);
            else
                return false;
        }
    }

    /**
     * Get a variable
     *
     * @param name the name
     * @return the value
     * @throws HGSEvalException HGSEvalException
     */
    public Object getVar(String name) throws HGSEvalException {
        Object v = map.get(name);
        if (v == null) {

            if (name.equals("output"))
                return toString();

            if (parent == null)
                throw new HGSEvalException("Variable not found: " + name);
            else
                return parent.getVar(name);
        } else
            return v;
    }

    /**
     * Set a variable
     *
     * @param name name
     * @param val  value
     * @return this for chained calls
     */
    public Context setVar(String name, Object val) {
        map.put(name, val);
        return this;
    }

    /**
     * Adds a function to the context.
     * Only needed for type checking. Calls setVar().
     *
     * @param name the name
     * @param func the function
     * @return this for chained calls
     */
    public Context addFunc(String name, Function func) {
        return setVar(name, func);
    }

    /**
     * Prints code to the context
     *
     * @param str the string to print
     * @return this for chained calls
     */
    public Context print(String str) {
        if (code != null)
            code.append(str);
        else
            parent.print(str);
        return this;
    }

    @Override
    public String toString() {
        if (code != null)
            return code.toString();
        else
            return parent.toString();
    }

    /**
     * @return the output length
     */
    public int length() {
        if (code != null)
            return code.length();
        else
            return parent.length();
    }

    /**
     * Flags this context as context belonging to a function call.
     * This allows to use the return statement.
     *
     * @return this for chained calls
     */
    public Context isFunctionContext() {
        functionContext = true;
        return this;
    }

    /**
     * Returns from a function call.
     *
     * @param returnValue the return value
     * @throws HGSEvalException HGSEvalException
     */
    public void returnFromFunc(Object returnValue) throws HGSEvalException {
        if (!functionContext)
            throw new HGSEvalException("The return statement is allowed only in a function!");

        throw new ReturnException(returnValue);
    }

    /**
     * Returns a function from this context
     *
     * @param funcName the functions name
     * @return the function
     * @throws HGSEvalException HGSEvalException
     */
    public FuncAdapter getFunction(String funcName) throws HGSEvalException {
        Object fObj = getVar(funcName);
        if (fObj instanceof FuncAdapter)
            return (FuncAdapter) fObj;
        else
            throw new HGSEvalException("Variable '" + funcName + "' is not a function");
    }

    private static final class FunctionPrint extends Function {

        private FunctionPrint() {
            super(-1);
        }

        @Override
        public Object callWithExpressions(Context c, ArrayList<Expression> args) throws HGSEvalException {
            for (Expression arg : args)
                c.print(arg.value(c).toString());
            return null;
        }
    }

    private static final class FunctionPrintf extends Function {

        private FunctionPrintf() {
            super(-1);
        }

        @Override
        public Object callWithExpressions(Context c, ArrayList<Expression> args) throws HGSEvalException {
            c.print(format(c, args));
            return null;
        }
    }

    private static final class FunctionFormat extends Function {

        private FunctionFormat() {
            super(-1);
        }

        @Override
        public Object callWithExpressions(Context c, ArrayList<Expression> args) throws HGSEvalException {
            return format(c, args);
        }
    }

    private static String format(Context c, ArrayList<Expression> args) throws HGSEvalException {
        if (args.size() < 2)
            throw new HGSEvalException("format/printf needs at least two arguments!");

        ArrayList<Object> eval = new ArrayList<>(args.size() - 1);
        for (int i = 1; i < args.size(); i++)
            eval.add(args.get(i).value(c));

        return String.format(Value.toString(args.get(0).value(c)), eval.toArray());
    }

    private static final class FunctionIsPresent extends Function {

        private FunctionIsPresent() {
            super(1);
        }

        @Override
        public Object callWithExpressions(Context c, ArrayList<Expression> args) {
            try {
                args.get(0).value(c);
                return true;
            } catch (HGSEvalException e) {
                return false;
            }
        }
    }

    private static final class FunctionPanic extends FuncAdapter {
        private FunctionPanic() {
            super(1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            throw new HGSEvalException(args[0].toString());
        }
    }

    /**
     * Exception used to return a value from a function
     */
    public static final class ReturnException extends HGSEvalException {
        private final Object returnValue;

        /**
         * Creates a new instance
         *
         * @param returnValue the return value
         */
        ReturnException(Object returnValue) {
            super("return");
            this.returnValue = returnValue;
        }

        /**
         * @return the return value
         */
        public Object getReturnValue() {
            return returnValue;
        }
    }
}
