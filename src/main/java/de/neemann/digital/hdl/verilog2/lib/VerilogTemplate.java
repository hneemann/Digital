/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog2.lib;

import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.hdl.model2.HDLException;
import de.neemann.digital.hdl.model2.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl2.Separator;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author ideras
 */
public class VerilogTemplate implements VerilogElement {

    private final static String MODULE_PREFIX = "DIG_";

    private final String moduleBaseName;
    private final Statement statements;
    private HashMap<String, Module> modules;

    /**
     * Creates a new instance
     *
     * @param elementName the element name
     * @param cl          the classloader used to load the template
     * @throws IOException  IOException
     * @throws HDLException HDLException
     */
    public VerilogTemplate(String elementName, ClassLoader cl) throws IOException, HDLException {
        super();
        this.moduleBaseName = MODULE_PREFIX + elementName;
        modules = new HashMap<>();

        try {
            statements = Parser.createFromJar(createFileName(moduleBaseName), cl);
        } catch (ParserException ex) {
            throw new HDLException(ex.getMessage());
        }

        if (statements == null) {
            throw new HDLException("Invalid verilog template file. Template is empty.");
        }
    }

    private static String createFileName(String name) {
        return "verilog/" + name + ".v";
    }

    /**
     * Creates the name of the file used to load the vhdl file for the given
     * element
     *
     * @param elementName the element name
     * @return the filename
     */
    public static String neededFileName(String elementName) {
        return createFileName(MODULE_PREFIX + elementName);
    }

    @Override
    public String print(CodePrinter out, HDLNode node) throws HGSEvalException, IOException {
        Module m = getModule(node);

        if (!m.isWritten) {
            out.println(m.code);
            m.setWritten();
        }

        return m.name;
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws IOException {
        try {
            Module m = getModule(node);

            List generics = m.getGenerics();

            if (generics == null || generics.isEmpty()) {
                return;
            }

            out.println("#(");
            out.inc();

            Separator comma = new Separator(out, ",\n");

            for (Object objv : generics) {
                String keyName;

                keyName = Value.toString(objv);

                Object keyVal = node.getElementAttributes().hgsMapGet(keyName);
                String kvs;
                if (keyVal instanceof Boolean) {
                    kvs = ((Boolean) keyVal) ? "1" : "0";
                } else {
                    kvs = keyVal.toString();
                }
                comma.check();
                out.print(".").print(keyName).print("(").print(kvs).print(")");
            }

            out.dec();
            out.println().println(")");
        } catch (HGSEvalException ex) {
            throw new IOException("error evaluating the template " + createFileName(moduleBaseName), ex);
        }
    }

    private Module getModule(HDLNode node) throws HGSEvalException {
        Module genModule = new Module(node, moduleBaseName);

        Module e = modules.get(genModule.name);
        if (e == null) {
            modules.put(genModule.name, genModule);
            return genModule;
        } else {
            if (!(genModule.code.equals(e.code)))
                throw new HGSEvalException(Lang.get("err_ifExternalComponentIsUsedTwiceCodeMustBeIdentical_N", genModule.name));
            else
                return e;
        }
    }

    private final class Module {
        private final String code;
        private String name;
        private final List generics;
        private boolean isWritten = false;

        private Module(HDLNode node, String name) throws HGSEvalException {
            this.name = name;
            final Context ctx = createRuntimeContext(node);

            try {
                statements.execute(ctx);
            } catch (HGSEvalException e) {
                throw new HGSEvalException("error evaluating hgs code " + name, e);
            }
            code = ctx.toString();

            if (ctx.contains("moduleName")) {
                this.name = Value.toString(ctx.getVar("moduleName"));
            }

            generics = (List) ctx.getVar("generics");
        }

        private Context createRuntimeContext(HDLNode node) throws HGSEvalException {
            Context ctx = new Context();

            ctx.declareVar("moduleName", name);
            ctx.declareVar("generics", new ArrayList<>());
            ctx.declareVar("elem", node.getElementAttributes());
            ctx.declareVar("inp", node.getInputs());
            ctx.declareVar("outp", node.getOutputs());

            return ctx;
        }

        private String getCode() {
            return code;
        }

        private boolean isWritten() {
            return isWritten;
        }

        private void setWritten() {
            isWritten = true;
        }

        private String getName() {
            return name;
        }

        private List getGenerics() {
            return generics;
        }
    }
}
