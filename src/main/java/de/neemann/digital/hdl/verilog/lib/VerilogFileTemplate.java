/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.lib;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.hdl.verilog.ir.stmt.VInstanceMapping;
import de.neemann.digital.hdl.verilog.ir.stmt.VInstanceBlock;
import de.neemann.digital.hdl.verilog.ir.stmt.VGenericMapping;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.hgs.HGSException;
import de.neemann.digital.hdl.hgs.HGSLexer;
import de.neemann.digital.hdl.hgs.HGSParser;
import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;
import de.neemann.digital.hdl.hgs.ast.stmt.Statement;
import de.neemann.digital.hdl.hgs.rt.ArrayValue;
import de.neemann.digital.hdl.hgs.rt.IntValue;
import de.neemann.digital.hdl.hgs.rt.RtReference;
import de.neemann.digital.hdl.hgs.rt.RtValue;
import de.neemann.digital.hdl.hgs.rt.StringValue;
import de.neemann.digital.hdl.hgs.rt.StructValue;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.expr.VIdExpr;
import de.neemann.digital.hdl.verilog.ir.VSignalDecl;
import de.neemann.digital.lang.Lang;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 *
 * @author ideras
 */
public class VerilogFileTemplate extends VerilogElement {
    private final static String MODULE_PREFIX = "DIG_";

    private final String moduleBaseName;
    private final ElementTypeDescription description;
    private final Statement cgenStmtModule;
    private boolean isStmtCompiled;

    /**
     * Creates a new instance
     *
     * @param elementName the element name
     * @param description the description
     * @throws IOException IOException
     * @throws HDLException HDLException
     */
    public VerilogFileTemplate(String elementName, ElementTypeDescription description) throws IOException, HDLException {
        super(description);
        this.moduleBaseName = MODULE_PREFIX + elementName;
        this.description = description;
        isStmtCompiled = false;

        try {
            cgenStmtModule = parseFile(moduleBaseName);

            if (cgenStmtModule == null) {
                throw  new HDLException("Invalid verilog template file. Template is empty.");
            }
        } catch (HGSException ex) {
            throw new HDLException(ex.getMessage());
        }
    }

    private Statement parseFile(String moduleName) throws IOException, HGSException {
        Statement stmt;
        String fileName = createFileName(moduleName);

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null)
            throw new IOException("file not present: " + fileName);

        try (BufferedInputStream in = new BufferedInputStream(inputStream)) {
            HGSLexer lexer = new HGSLexer(in);
            HGSParser parser = new HGSParser(lexer);

            stmt = parser.parse();
        }

        return stmt;
    }

    private static String createFileName(String name) {
        return "verilog/" + name + ".v";
    }

    private Key findKey(String name) throws HDLException {
        for (Field k : Keys.class.getDeclaredFields()) {
            if (Modifier.isStatic(k.getModifiers()) && Key.class.isAssignableFrom(k.getType())) {
                try {
                    Key key = (Key) k.get(null);
                    if (key.getKey().equals(name))
                        return key;
                } catch (IllegalAccessException e) {
                    throw new HDLException("invalid generic key " + name, e);
                }
            }
        }
        throw new HDLException("invalid generic key " + name);
    }

    @Override
    public void buildCodeIr(VerilogCodeBuilder vcBuilder, HDLNode node) throws HDLException {
        HGSRuntimeContext ctx = createRuntimeContext(node);
        String moduleName = moduleBaseName;

        try {
            cgenStmtModule.execute(ctx);

            if (ctx.containsVariable("moduleName")) {
                RtValue v = ctx.getVariableValue("moduleName");
                if (!v.isString()) {
                    throw new HDLException(Lang.get("moduleNameVarNotString"));
                }
                moduleName = ((StringValue) v).getValue();
            }

            if (!vcBuilder.isModuleRegistered(moduleName)) {
                vcBuilder.registerModule(moduleName, ctx.getOutput());
            }
        } catch (HGSException ex) {
            throw new HDLException(ex.getMessage());
        }

        ArrayList<VGenericMapping> genericMappings = null;

        if (ctx.containsVariable("generics")) {
            RtValue v = ctx.getVariableValue("generics");

            if (!v.isArray()) {
                throw new HDLException(Lang.get("genericVarNotArray"));
            }
            ArrayValue arrv = (ArrayValue) v;
            genericMappings = new ArrayList<>();

            for (RtReference refv : arrv) {
                v = refv.getTarget();

                if (!v.isString()) {
                    throw new HDLException(Lang.get("genericElemNotString"));
                }
                String keyName = ((StringValue) v).getValue();

                Key key = new Key<>(keyName, 0);
                if (!node.getAttributes().contains(key)) {
                    // ensures the usage of the correct default value
                    key = findKey(keyName);
                }
                genericMappings.add(new VGenericMapping(keyName, node.get(key).toString()));
            }

        }

        ArrayList<VInstanceMapping> signalMappings = new ArrayList<>();

        String instName = vcBuilder.getNextName(moduleName);

        VInstanceBlock instStmt = new VInstanceBlock(moduleName, instName, genericMappings, signalMappings);

        for (Port p : node.getPorts().getInputs()) {
            VIRNode irnode = vcBuilder.getSignalCodeIr(p.getSignal());
            VExpr inExpr = irnode.resolveToExpr(vcBuilder);
            inExpr = inExpr.resolveToIdExpr(vcBuilder);

            signalMappings.add(new VInstanceMapping(p.getName(), inExpr));
        }

        for (Port p : node.getPorts().getOutputs()) {
            Signal s = p.getSignal();

            if (s == null) {
                continue;
            }
            VExpr expr = new VIdExpr(s.getName());
            signalMappings.add(new VInstanceMapping(p.getName(), expr));

            vcBuilder.registerAndAddSignalDecl(s, VSignalDecl.Type.WIRE);
            expr.setSignal(s);
            vcBuilder.setCodeIrForSignal(s, expr);
        }
        vcBuilder.registerStatement(instStmt, null);
    }

    private void setAttrs(HDLNode node, HGSRuntimeContext ctx) {
        StructValue elemStruct = new StructValue();
        elemStruct.setFieldValue("name", moduleBaseName);

        ElementAttributes attrs = node.getAttributes();

        elemStruct.setFieldValue("bits", node.get(Keys.BITS));
        elemStruct.setFieldValue("addr_bits", node.get(Keys.ADDR_BITS));

        if (attrs.contains(Keys.DESCRIPTION)) {
            elemStruct.setFieldValue("description", node.get(Keys.DESCRIPTION));
        }
        if (attrs.contains(Keys.DATA)) {
            long[] data = node.get(Keys.DATA).getMinimized().getData();

            ArrayValue vdata = new ArrayValue(data.length);
            for (int i = 0; i < data.length; i++) {
                vdata.set(i, new IntValue((int) data[i]));
            }
            elemStruct.setFieldValue("data", vdata);
        }

        ctx.setVariableValue("elem", elemStruct);
    }

    private HGSRuntimeContext createRuntimeContext(HDLNode node) {
        HGSRuntimeContext ctx = new HGSRuntimeContext();

        setAttrs(node, ctx);

        ArrayValue inPorts = new ArrayValue(node.getPorts().getInputs().size());
        ArrayValue inSignals = new ArrayValue(node.getPorts().getInputs().size());
        int index = 0;
        for (Port p : node.getPorts().getInputs()) {
            StructValue signalStruct = new StructValue();
            StructValue portStruct = new StructValue();

            signalStruct.setFieldValue("name", p.getSignal().getName());
            signalStruct.setFieldValue("bits", p.getSignal().getBits());

            portStruct.setFieldValue("orig_name", p.getOrigName());
            portStruct.setFieldValue("name", p.getName());
            portStruct.setFieldValue("bits", p.getBits());

            inSignals.set(index, signalStruct);
            inPorts.set(index, portStruct);

            index++;
        }
        ctx.setVariableValue("ins", inSignals);
        ctx.setVariableValue("inp", inPorts);

        ArrayValue outSignals = new ArrayValue(node.getPorts().getOutputs().size());
        ArrayValue outPorts = new ArrayValue(node.getPorts().getOutputs().size());
        index = 0;
        for (Port p : node.getPorts().getOutputs()) {
            StructValue signalStruct = null;
            StructValue portStruct = new StructValue();

            if (p.getSignal() != null) {
                signalStruct = new StructValue();
                signalStruct.setFieldValue("name", p.getSignal().getName());
                signalStruct.setFieldValue("bits", p.getSignal().getBits());
            }
            portStruct.setFieldValue("orig_name", p.getOrigName());
            portStruct.setFieldValue("name", p.getName());
            portStruct.setFieldValue("bits", p.getBits());

            outSignals.set(index, signalStruct);
            outPorts.set(index, portStruct);
            index++;
        }
        ctx.setVariableValue("outs", outSignals);
        ctx.setVariableValue("outp", outPorts);

        return ctx;
    }
}
