/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.rt;

import de.neemann.digital.hdl.hgs.HGSException;
import java.util.HashMap;

/**
 *
 * @author ideras
 */
public class HGSRuntimeContext {
    private final HashMap<String, RtReference> varMap;
    private StringBuilder outSb;

    /**
     * Creates a new intance.
     */
    public HGSRuntimeContext() {
        varMap = new HashMap<>();
        outSb = new StringBuilder();
    }

    /**
     * Register a new variable with the specified type.
     *
     * @param varName the variable name.
     * @param type the variable type.
     */
    public void registerVariable(String varName, RtValue.Type type) {
        if (!varMap.containsKey(varName)) {
            varMap.put(varName, new RtReference(type));
        }
    }

    /**
     * Sets a variable value.
     *
     * @param varName the variable name.
     * @param value the new variable value.
     */
    public void setVariableValue(String varName, RtValue value) {
        registerVariable(varName, value.getType());
        RtReference vref = varMap.get(varName);

        vref.set(value);
    }

    /**
     * Checks if a variable exists.
     *
     * @param varName the variable name.
     * @return true if the variable exists, false otherwise.
     */
    public boolean containsVariable(String varName) {
        return varMap.containsKey(varName);
    }

    /**
     * Return the variable value.
     *
     * @param varName the variable name.
     * @return the variable value or null is the variable doesn't exist.
     */
    public RtValue getVariableValue(String varName) {
        RtReference vref = varMap.get(varName);

        return (vref == null)? null : vref.getTarget();
    }

    /**
     * Return a variable reference.
     *
     * @param varName the variable name.
     * @return the variable reference or null if the variable doesn't exist.
     */
    public RtReference getVariableRef(String varName) {
        return varMap.get(varName);
    }

    /**
     * Print a RtValue object.
     *
     * @param objValue the value to print.
     * @return "this" reference to allow call chaining.
     * @throws HGSException  HDLGenException
     */
    public HGSRuntimeContext print(RtValue objValue) throws HGSException {

        if (objValue instanceof IntValue) {
            outSb.append(((IntValue) objValue).getValue());
        } else if (objValue instanceof StringValue) {
            outSb.append(((StringValue) objValue).getValue());
        } else {
           outSb.append(objValue.toString());
        }
        return this;
    }

    /**
     * Prints an integer value.
     *
     * @param value the value to print.
     * @return "this" reference to allow call chaining.
     */
    public HGSRuntimeContext print(int value) {
        outSb.append(value);

        return this;
    }

    /**
     * Prints a string value.
     *
     * @param str the value to print.
     * @return "this" reference to allow call chaining.
     */
    public HGSRuntimeContext print(String str) {
        outSb.append(str);

        return this;
    }

    /**
     * Returns the output as string.
     *
     * @return the output.
     */
    public String getOutput() {
        return outSb.toString();
    }

    /**
     * Clear the output.
     */
    public void clearOutput() {
        outSb = new StringBuilder();
    }
}
