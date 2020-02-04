/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import de.neemann.digital.core.Bits;
import de.neemann.digital.hdl.hgs.function.Func;
import de.neemann.digital.hdl.hgs.function.Function;
import de.neemann.digital.hdl.hgs.function.InnerFunction;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The evaluation context
 */
public class Context implements HGSMap {
    private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);
    // declare some functions which are always present
    private static final HashMap<String, InnerFunction> BUILT_IN = new HashMap<>();

    static {
        BUILT_IN.put("bitsNeededFor", new FunctionBitsNeeded());
        BUILT_IN.put("ceil", new FunctionCeil());
        BUILT_IN.put("floor", new FunctionFloor());
        BUILT_IN.put("round", new FunctionRound());
        BUILT_IN.put("float", new FunctionFloat());
        BUILT_IN.put("int", new FunctionInt());
        BUILT_IN.put("min", new FunctionMin());
        BUILT_IN.put("max", new FunctionMax());
        BUILT_IN.put("abs", new FunctionAbs());
        BUILT_IN.put("print", new FunctionPrint());
        BUILT_IN.put("printf", new FunctionPrintf());
        BUILT_IN.put("println", new FunctionPrintln());
        BUILT_IN.put("log", new FunctionLog());
        BUILT_IN.put("format", new FunctionFormat());
        BUILT_IN.put("isPresent", new FunctionIsPresent());
        BUILT_IN.put("panic", new FunctionPanic());
        BUILT_IN.put("output", new FunctionOutput());
        BUILT_IN.put("splitString", new FunctionSplitString());
        BUILT_IN.put("identifier", new FunctionIdentifier());
        BUILT_IN.put("sizeOf", new Func(1, args -> Value.toArray(args[0]).hgsArraySize()));
        BUILT_IN.put("newMap", new Func(0, args -> new HashMap()));
        BUILT_IN.put("newList", new Func(0, args -> new ArrayList()));
    }

    private final Context parent;
    private final StringBuilder code;
    private final HashMap<String, Object> map;
    private boolean loggingEnabled = true;

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
            if (parent == null) {
                InnerFunction builtIn = BUILT_IN.get(name);
                if (builtIn != null)
                    return builtIn;

                throw new HGSEvalException("Variable not found: " + name);
            } else
                return parent.getVar(name);
        } else
            return v;
    }

    /**
     * Set a variable.
     * This method is not able to create a new variable.
     *
     * @param name name
     * @param val  value
     * @throws HGSEvalException HGSEvalException
     */
    public void setVar(String name, Object val) throws HGSEvalException {
        Object v = map.get(name);
        if (v != null) {
            if (v.getClass().isAssignableFrom(val.getClass()))
                map.put(name, val);
            else
                throw new HGSEvalException("Variable '" + name + "' has wrong type. Needs to be "
                        + v.getClass().getSimpleName() + ", is " + val.getClass().getSimpleName());
        } else {
            if (parent != null)
                parent.setVar(name, val);
            else
                throw new HGSEvalException("Variable '" + name + "' not declared!");
        }
    }


    /**
     * Exports a new variable.
     * Exporting means the value is declared in the root context.
     *
     * @param name  the name of the variable
     * @param value the value of the variable
     * @return this for chained calls
     * @throws HGSEvalException HGSEvalException
     */
    public Context exportVar(String name, Object value) throws HGSEvalException {
        if (parent == null)
            declareVar(name, value);
        else
            parent.exportVar(name, value);
        return this;
    }

    /**
     * Declares a new variable
     *
     * @param name  the name of the variable
     * @param value the value of the variable
     * @return this for chained calls
     * @throws HGSEvalException HGSEvalException
     */
    public Context declareVar(String name, Object value) throws HGSEvalException {
        if (map.containsKey(name))
            throw new HGSEvalException("Variable '" + name + "' already declared!");
        map.put(name, value);
        return this;
    }

    /**
     * Adds a function to the context.
     * Only needed for type checking. Calls setVar().
     *
     * @param name the name
     * @param func the function
     * @return this for chained calls
     * @throws HGSEvalException HGSEvalException
     */
    public Context declareFunc(String name, InnerFunction func) throws HGSEvalException {
        return declareVar(name, func);
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
     * Clears the output of this context.
     */
    public void clearOutput() {
        if (code != null)
            code.setLength(0);
        else
            parent.clearOutput();

    }

    /**
     * Logs a message
     *
     * @param o the object to log
     */
    private void log(Object o) {
        if (loggingEnabled) {
            if (parent != null)
                parent.log(o);
            else
                LOGGER.info(o.toString());
        }
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
     * Disables the logging in this context.
     *
     * @return this for chained calls
     */
    public Context disableLogging() {
        loggingEnabled = false;
        return this;
    }

    /**
     * Returns a function from this context.
     * A helper function to obtain a function from java code.
     *
     * @param funcName the functions name
     * @return the function
     * @throws HGSEvalException HGSEvalException
     */
    public Function getFunction(String funcName) throws HGSEvalException {
        Object fObj = getVar(funcName);
        if (fObj instanceof Function)
            return (Function) fObj;
        else
            throw new HGSEvalException("Variable '" + funcName + "' is not a function");
    }

    @Override
    public Object hgsMapGet(String key) {
        return map.get(key);
    }

    private static final class FunctionPrint extends InnerFunction {
        private FunctionPrint() {
            super(-1);
        }

        @Override
        public Object call(Context c, ArrayList<Expression> args) throws HGSEvalException {
            for (Expression arg : args)
                c.print(arg.value(c).toString());
            return null;
        }
    }

    private static final class FunctionPrintln extends InnerFunction {
        private FunctionPrintln() {
            super(-1);
        }

        @Override
        public Object call(Context c, ArrayList<Expression> args) throws HGSEvalException {
            for (Expression arg : args)
                c.print(arg.value(c).toString());
            c.print(System.lineSeparator());
            return null;
        }
    }

    private static final class FunctionPrintf extends InnerFunction {
        private FunctionPrintf() {
            super(-1);
        }

        @Override
        public Object call(Context c, ArrayList<Expression> args) throws HGSEvalException {
            c.print(format(c, args));
            return null;
        }
    }

    private static final class FunctionFormat extends InnerFunction {
        private FunctionFormat() {
            super(-1);
        }

        @Override
        public Object call(Context c, ArrayList<Expression> args) throws HGSEvalException {
            return format(c, args);
        }
    }

    private static String format(Context c, ArrayList<Expression> args) throws HGSEvalException {
        if (args.size() < 2)
            throw new HGSEvalException("format/printf needs at least two arguments!");

        ArrayList<Object> eval = new ArrayList<>(args.size() - 1);
        for (int i = 1; i < args.size(); i++)
            eval.add(args.get(i).value(c));

        return String.format(Locale.US, Value.toString(args.get(0).value(c)), eval.toArray());
    }

    private static final class FunctionIsPresent extends InnerFunction {
        private FunctionIsPresent() {
            super(1);
        }

        @Override
        public Object call(Context c, ArrayList<Expression> args) {
            try {
                args.get(0).value(c);
                return true;
            } catch (HGSEvalException e) {
                return false;
            }
        }
    }

    private static final class FunctionPanic extends Function {
        private FunctionPanic() {
            super(-1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            if (args.length == 0)
                throw new HGSEvalException("panic");

            String message = args[0].toString();
            if (message.startsWith("err_")) {
                if (args.length == 1)
                    message = Lang.get(message);
                else {
                    Object[] ar = new String[args.length - 1];
                    for (int i = 0; i < args.length - 1; i++)
                        ar[i] = args[i + 1].toString();
                    message = Lang.get(message, ar);
                }
            }

            throw new HGSEvalException(message);
        }
    }

    private static final class FunctionOutput extends InnerFunction {
        private FunctionOutput() {
            super(0);
        }

        @Override
        public Object call(Context c, ArrayList<Expression> args) {
            return c.toString();
        }
    }

    private static final class FunctionCeil extends Function {
        private FunctionCeil() {
            super(1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            if (args[0] instanceof Double)
                return (long) Math.ceil((Double) args[0]);
            return Value.toLong(args[0]);
        }
    }

    private static final class FunctionFloor extends Function {
        private FunctionFloor() {
            super(1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            if (args[0] instanceof Double)
                return (long) Math.floor((Double) args[0]);
            return Value.toLong(args[0]);
        }
    }

    private static final class FunctionRound extends Function {
        private FunctionRound() {
            super(1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            if (args[0] instanceof Double)
                return Math.round((Double) args[0]);
            return Value.toLong(args[0]);
        }
    }

    private static final class FunctionFloat extends Function {
        private FunctionFloat() {
            super(1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            return Value.toDouble(args[0]);
        }
    }

    private static final class FunctionInt extends Function {
        private FunctionInt() {
            super(1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            return Value.toInt(args[0]);
        }
    }

    private static final class FunctionBitsNeeded extends Function {

        private FunctionBitsNeeded() {
            super(1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            return Bits.binLn2(Value.toLong(args[0]));
        }
    }

    private static final class FunctionAbs extends Function {

        private FunctionAbs() {
            super(1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            if (args[0] instanceof Double)
                return Math.abs((Double) args[0]);

            return Math.abs(Value.toLong(args[0]));
        }
    }

    private static final class FunctionSplitString extends Function {

        private FunctionSplitString() {
            super(1);
        }

        @Override
        protected Object f(Object... args) {
            StringTokenizer st = new StringTokenizer(args[0].toString(), " \r\t\n,:;");
            ArrayList<String> list = new ArrayList<>();
            while (st.hasMoreTokens())
                list.add(st.nextToken());
            return list;
        }
    }

    private static final class FunctionIdentifier extends Function {

        private FunctionIdentifier() {
            super(1);
        }

        @Override
        protected Object f(Object... args) {
            String str = args[0].toString();
            StringBuilder sb = new StringBuilder(str.length());
            for (int p = 0; p < str.length(); p++) {
                char c = str.charAt(p);
                if (c >= '0' && c <= '9') {
                    if (sb.length() == 0)
                        sb.append('n');
                    sb.append(c);
                } else if ((c >= 'A' && c <= 'Z')
                        || (c >= 'a' && c <= 'z')
                        || c == '_') {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }

    private static final class FunctionLog extends InnerFunction {

        private FunctionLog() {
            super(1);
        }


        @Override
        public Object call(Context c, ArrayList<Expression> args) throws HGSEvalException {
            if (args.size() != 1)
                throw new HGSEvalException("wrong number of arguments! found: " + args.size() + ", expected: " + getArgCount());
            Object v = args.get(0).value(c);
            c.log(v);
            return v;
        }
    }

    private static final class FunctionMin extends Function {
        private FunctionMin() {
            super(-1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            long minL = Long.MAX_VALUE;
            double minD = Double.MAX_VALUE;
            for (Object v : args) {
                if (v instanceof Double) {
                    double l = (Double) v;
                    if (minD > l) minD = l;
                } else {
                    long l = Value.toLong(v);
                    if (minL > l) minL = l;
                }
            }

            if (minD < Double.MAX_VALUE && minL < Long.MAX_VALUE) {
                return Math.min(minD, minL);
            } else if (minD < Double.MAX_VALUE)
                return minD;
            else
                return minL;
        }
    }

    private static final class FunctionMax extends Function {
        private FunctionMax() {
            super(-1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            long maxL = Long.MIN_VALUE;
            double maxD = -Double.MAX_VALUE;
            for (Object v : args) {
                if (v instanceof Double) {
                    double l = (Double) v;
                    if (maxD < l) maxD = l;
                } else {
                    long l = Value.toLong(v);
                    if (maxL < l) maxL = l;
                }
            }

            if (maxD > -Double.MAX_VALUE && maxL > Long.MIN_VALUE) {
                return Math.max(maxD, maxL);
            } else if (maxD > -Double.MAX_VALUE)
                return maxD;
            else
                return maxL;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Context context = (Context) o;
        return Objects.equals(parent, context.parent)
                && map.equals(context.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, map);
    }
}
