/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.stmt;

import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.vhdl.Separator;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents a verilog component instantiation
 * 
 * @author ideras
 */
public class VInstanceBlock  extends VStatement {
    private final String componentName;
    private final String instanceName;
    private final ArrayList<VInstanceMapping> signalMappings;
    private final ArrayList<VGenericMapping> genericMappings;

    /**
     * Creates a new instance
     *
     * @param componentName the name of the component
     * @param instanceName the name of the instance
     * @param genericMappings the list of generic mappings
     * @param signalMappings the list of signal mappings
     */
    public VInstanceBlock(String componentName, String instanceName,
                          ArrayList<VGenericMapping> genericMappings,
                          ArrayList<VInstanceMapping> signalMappings) {
        super(null);
        this.componentName = componentName;
        this.instanceName = instanceName;
        this.signalMappings = signalMappings;
        this.genericMappings = genericMappings;
    }

    /**
     * Creates a new instance without generics
     *
     * @param componentName the name of the component
     * @param instanceName the name of the instance
     * @param signalMappings the list of signal mappings
     */
    public VInstanceBlock(String componentName, String instanceName, ArrayList<VInstanceMapping> signalMappings) {
        this(componentName, instanceName, null, signalMappings);
    }

    /**
     * Returns the component name
     *
     * @return the component name
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * Returns the instance name
     *
     * @return the instance name
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Return the list of signal mappings
     *
     * @return the list of signal mappings
     */
    public ArrayList<VInstanceMapping> getSignalMappings() {
        return signalMappings;
    }

    @Override
    public void writeSourceCode(VerilogCodeBuilder vcBuilder, CodePrinter out) throws IOException {
        out.print(componentName).print(" ");
        Separator comma = new Separator(",\n");

        if (genericMappings != null) {
            out.print("#(").println();
            out.inc();
            for (VGenericMapping g : genericMappings) {
                comma.check(out);
                out.print(".").print(g.getName()).print("(").print(g.getValue()).print(")");
            }
            out.dec();
            out.println().print(") ");
        }
        out.print(instanceName).println(" (");
        out.inc();

        comma = new Separator(",\n");
        for (VInstanceMapping m : signalMappings) {
            comma.check(out);
            out.print(".").print(m.getSignalName()).print("(")
               .print(m.getArgExpr().getSourceCode(vcBuilder)).print(")");
        }
        out.dec();
        out.println().print(");");
    }

}
