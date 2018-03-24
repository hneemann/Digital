/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.hdl.model2.HDLException;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.printer.CodePrinterStr;

import java.io.Closeable;
import java.io.IOException;

public class VHDLGenerator implements Closeable {

    private final ElementLibrary library;
    private final CodePrinter out;

    /**
     * Creates a new exporter
     *
     * @param library the library
     * @throws IOException IOException
     */
    public VHDLGenerator(ElementLibrary library) throws IOException {
        this(library, new CodePrinterStr());
    }

    /**
     * Creates a new exporter
     *
     * @param library the library
     * @param out     the output stream
     * @throws IOException IOException
     */
    public VHDLGenerator(ElementLibrary library, CodePrinter out) throws IOException {
        this.library = library;
        this.out = out;
    }

    public VHDLGenerator export(Circuit circuit) throws PinException, HDLException, NodeException, IOException, HGSEvalException {
        HDLModel model = new HDLModel(library).create(circuit);
        for (HDLCircuit c : model)
            c.mergeOperations().nameNets(new HDLCircuit.SimpleNaming());

        model.rename(name -> {
            if (VHDLKeywords.isKeyword(name) || Character.isDigit(name.charAt(0)))
                return "p_" + name;
            else {
                switch (name) {
                    case "=":
                        return "eq";
                    case "<":
                        return "le";
                    case ">":
                        return "gr";
                    default:
                        return name;
                }
            }
        });

        new VHDLCreator(out, model).printHDLCircuit(model.getMain());


        return this;
    }

    @Override
    public String toString() {
        return out.toString();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
