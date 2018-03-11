/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.expr;

import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.rt.IntValue;
import de.neemann.digital.hdl.hgs.rt.RtValue;
import de.neemann.digital.hdl.hgs.rt.StringValue;
import de.neemann.digital.lang.Lang;
import java.util.ArrayList;
import java.util.MissingFormatArgumentException;

/**
 *
 * @author ideras
 */
public class FunctionCallExpr extends Expr {
    private final String functionName;
    private ArrayList<Expr> arguments;

    /**
     * Creates a new instance
     *
     * @param line the source line
     * @param functionName the function name to call
     * @param arguments the list of arguments
     */
    public FunctionCallExpr(int line, String functionName, ArrayList<Expr> arguments) {
        super(line);
        this.functionName = functionName;
        this.arguments = arguments;
    }

    /**
     * Returns the function name
     *
     * @return the function name
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Returns the list of arguments
     *
     * @return the list of arguments
     */
    public ArrayList<Expr> getArguments() {
        return arguments;
    }

    @Override
    public RtValue evaluate(HGSRuntimeContext ctx) throws HGSException {
        if (functionName.equals("isset")) {
            if (arguments.size() != 1) {
                throw new HGSException(getLine(),
                                          "Invalid argument count in call to function 'isset'. Expected 1, found " + arguments.size());
            }
            int result;

            try {
                RtValue val = arguments.get(0).evaluate(ctx);
                result = (val != null)? 1 : 0;
            } catch (HGSException ex) {
                result = 0;
            }

            return new IntValue(result);
        } else if (functionName.equals("format")) {
            RtValue argv = arguments.get(0).evaluate(ctx);

            if (!argv.isString()) {
                throw new HGSException(getLine(), Lang.get("firstArgFormatNotString"));
            }
            String fmtStr = ((StringValue) argv).getValue();

            Object[] args = new Object[arguments.size() - 1];

            for (int i = 1; i < arguments.size(); i++) {
                RtValue objVal = arguments.get(i).evaluate(ctx);
                if (objVal instanceof IntValue) {
                    args[i-1] = ((IntValue) objVal).getValue();
                } else if (objVal instanceof StringValue) {
                    args[i-1] = ((StringValue) objVal).getValue();
                } else {
                    throw new HGSException(getLine(), Lang.get("invalidArgInFormatFn"));
                }
            }
            try {
                String resultStr = String.format(fmtStr, args);

                return new StringValue(resultStr);
            } catch (MissingFormatArgumentException ex) {
                throw new HGSException(getLine(), "Missing argument in printf. " + ex.getLocalizedMessage());
            } catch (IllegalArgumentException ex) {
                throw new HGSException(getLine(), "Illegal argument in printf. " + ex.getLocalizedMessage());
            }

        } else {
            throw new HGSException(getLine(), Lang.get("invalidFunction", functionName));
        }
    }

}
