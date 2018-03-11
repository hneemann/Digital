/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.stmt;

import de.neemann.digital.hdl.hgs.ast.expr.Expr;
import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.rt.IntValue;
import de.neemann.digital.hdl.hgs.rt.RtValue;
import de.neemann.digital.hdl.hgs.rt.StringValue;
import java.util.ArrayList;
import java.util.MissingFormatArgumentException;

/**
 *
 * @author ideras
 */
public class PrintfStatement extends Statement {
    private final String formatString;
    private final ArrayList<Expr> arguments;

    /**
     * Create a new instance
     *
     * @param line the source line
     * @param formatString the format string
     * @param arguments the statement arguments
     */
    public PrintfStatement(int line, String formatString, ArrayList<Expr> arguments) {
        super(line);
        this.formatString = formatString;
        this.arguments = arguments;
    }

    /**
     * Returns the format string
     *
     * @return the format string
     */
    public String getFormatStr() {
        return formatString;
    }

    /**
     * Return the argument list
     *
     * @return the argument list
     */
    public ArrayList<Expr> getArguments() {
        return arguments;
    }

    @Override
    public void execute(HGSRuntimeContext ctx) throws HGSException {
        String formatStr = formatString.replace("\\n", System.lineSeparator());

        if (arguments != null) {
            Object[] args = new Object[arguments.size()];
            for (int i = 0; i < arguments.size(); i++) {
                RtValue objVal = arguments.get(i).evaluate(ctx);
                if (objVal instanceof IntValue) {
                    args[i] = ((IntValue) objVal).getValue();
                } else if (objVal instanceof StringValue) {
                    args[i] = ((StringValue) objVal).getValue();
                } else {
                    throw new HGSException(getLine(), "Invalid argument in printf statement.");
                }
            }
            try {
                ctx.print(String.format(formatStr, args));
            } catch (MissingFormatArgumentException ex) {
                throw new HGSException(getLine(), "Missing argument in printf. " + ex.getLocalizedMessage());
            } catch (IllegalArgumentException ex) {
                throw new HGSException(getLine(), "Illegal argument in printf. " + ex.getLocalizedMessage());
            }
        } else {
            // We use String.format to interpret character sequences like \n
            try {
                ctx.print(String.format(formatStr));
            } catch (MissingFormatArgumentException ex) {
                throw new HGSException(getLine(), "Missing argument in printf. " + ex.getLocalizedMessage());
            } catch (IllegalArgumentException ex) {
                throw new HGSException(getLine(), "Illegal argument in printf. " + ex.getLocalizedMessage());
            }
        }
    }

}
