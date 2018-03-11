/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLModel;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.ir.stmt.VAssignStatement;
import de.neemann.digital.hdl.verilog.ir.stmt.VEmptyStatement;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.expr.VIdExpr;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatement;
import de.neemann.digital.hdl.verilog.ir.VSignalDecl;
import de.neemann.digital.hdl.verilog.ir.expr.VDelegatedExpr;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatementPlace;
import de.neemann.digital.hdl.verilog.lib.VerilogElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author ideras
 */
public class VerilogCodeBuilder {
    private final HDLModel model;
    private final ElementLibrary library;
    private final VerilogLibrary vlibrary;
    private final HashMap<String, VIRNode> codeIrMap;
    private final HashMap<Signal, HDLNode> outputSignalMap;
    private final HashSet<HDLNode> visitedNodes;
    private final HashSet<Signal> visitedSignals;
    private final ArrayList<VStatement> statements;
    private final ArrayList<VStatement> initialStatements;
    private final ModuleList modules;
    private final ArrayList<VSignalDecl> decls;
    private final HashMap<String, VSignalDecl> registeredSignals;
    private final HashSet<String> declaredSignals;
    private final HashMap<String, Integer> genNames;

    /**
     * Creates a new code builder instance
     *
     * @param model    the HDL model
     * @param modules  the list of generated modules.
     * @param library  the element library
     * @param vlibrary the verilog element library
     */
    public VerilogCodeBuilder(HDLModel model, ModuleList modules, ElementLibrary library, VerilogLibrary vlibrary) {
        this.model = model;
        this.library = library;
        this.vlibrary = vlibrary;
        codeIrMap = new HashMap<>();
        outputSignalMap = new HashMap<>();
        visitedNodes = new HashSet<>();
        visitedSignals = new HashSet<>();
        statements = new ArrayList<>();
        initialStatements = new ArrayList<>();
        this.modules = modules;
        decls = new ArrayList<>();
        declaredSignals = new HashSet<>();
        registeredSignals = new HashMap<>();
        genNames = new HashMap<>();
    }

    /**
     * Creates a new code builder instance
     *
     * @param model    the HDL model
     * @param library  the element library
     * @param vlibrary the verilog element library
     */
    public VerilogCodeBuilder(HDLModel model, ElementLibrary library, VerilogLibrary vlibrary) {
        this(model, null, library, vlibrary);
    }

    /**
     * Register the code for input signals in the model.
     */
    public void registerModelInputs() {
        for (Port p : model.getPorts().getInputs()) {
            Signal s = p.getSignal();

            VExpr expr = new VIdExpr(p.getName());
            expr.setSignal(s);
            setCodeIrForSignal(p.getName(), expr);
        }
    }

    /**
     * Return the HDL node associated with the signal.
     *
     * @param outs the output signal.
     * @return HDLNode
     */
    public HDLNode getHDLNode(Signal outs) {
        return outputSignalMap.get(outs);
    }

    /**
     * Check if a node is visited
     *
     * @param node the node
     * @return true is the node is visited, false otherwise
     */
    private boolean isNodeVisited(HDLNode node) {
        return visitedNodes.contains(node);
    }

    /**
     * Mark a node as visited.
     *
     * @param node the node
     */
    private void markNodeVisited(HDLNode node) {
        visitedNodes.add(node);
    }

    /**
     * Checks if a signal has been visited.
     *
     * @param s the signal
     * @return {@code true} if the signal has been visited, {@code false} otherwise.
     */
    private boolean isSignalVisited(Signal s) {
        return visitedSignals.contains(s);
    }

    /**
     * Mark a signal visited
     *
     * @param s the signal
     */
    private void markSignalVisited(Signal s) {
        visitedSignals.add(s);
    }

    /**
     * Return the registered code IR (if any) for a signal
     *
     * @param signalName the signal name
     * @return the code IR
     */
    public VIRNode getSignalCodeIr(String signalName) {
        return codeIrMap.get(signalName);
    }

    /**
     * Return the registered code IR (if any) for a signal
     *
     * @param signal the signal
     * @return the code IR
     */
    public VIRNode getSignalCodeIr(Signal signal) {
        return codeIrMap.get(signal.getName());
    }

    /**
     * Register a code IR for a signal.
     *
     * @param signalName the signal name
     * @param codeIr the code IR
     */
    public void setCodeIrForSignal(String signalName, VIRNode codeIr) {
        if (signalName.equals("S25")) {
            System.out.println("S25");
        }
        if (codeIrMap.containsKey(signalName)) {
            // The signal has been registered already, this can happen when
            // there is a loop between components
            VIRNode n = getSignalCodeIr(signalName);

            if (n instanceof VEmptyStatement) {
                VEmptyStatement dstmt = (VEmptyStatement) n;


            } else if (n instanceof VDelegatedExpr) {
                VDelegatedExpr dexpr = (VDelegatedExpr) n;

                statements.remove(dexpr.getStatement());
                if (codeIr.isExpr()) {
                    if (!(codeIr instanceof VIdExpr)) {
                        VStatement stmt = new VAssignStatement(dexpr.getStatement().getPlace(), (VExpr) codeIr);

                        statements.add(stmt);
                    }
                    registerAndAddSignalDecl(dexpr.getSignal(), VSignalDecl.Type.WIRE);
                } else if (codeIr.isStatement()) {
                    VStatement stmt = (VStatement) codeIr;
                    VSignalDecl.Type stype = VSignalDecl.Type.WIRE;

                    if (!(stmt instanceof VAssignStatement)) {
                        stype = VSignalDecl.Type.REG;
                    }

                    registerAndAddSignalDecl(dexpr.getSignal(), stype);
                    statements.add(stmt);
                } else {
                    throw new RuntimeException("BUG in the machine: Invalid code IR node '" + codeIr.getClass().toString() + "'");
                }
            } else {
                throw new RuntimeException("BUG in the machine: Called twice to setCodeIR");
            }
        }

        codeIrMap.put(signalName, codeIr);
    }

    /**
     * Register a code IR for a signal.
     *
     * @param signal the signal
     * @param codeIr the code IR
     */
    public void setCodeIrForSignal(Signal signal, VIRNode codeIr) {
        setCodeIrForSignal(signal.getName(), codeIr);
    }

    /**
     * Register a statement with the builder
     *
     * @param statement the statement
     * @param signal the signal associated with the statement
     */
    public void registerStatement(VStatement statement, Signal signal) {
        if (!statements.contains(statement)) {
            statements.add(statement);

            if (signal != null) {
                codeIrMap.remove(signal.getName());
            }
        }
    }

    /**
     * Add a statement to the initial block
     *
     * @param statement the statement
     */
    public void addInitialStatement(VStatement statement) {
        initialStatements.add(statement);
    }

    /**
     * Register a module with the builder
     *
     * @param moduleName the module name
     * @param moduleCode the generated module code
     */
    public void registerModule(String moduleName, String moduleCode) {
        modules.registerModule(moduleName, moduleCode);
    }

    /**
     * Checks if a module is registered.
     *
     * @param moduleName the module name
     * @return true is the module is registered, false otherwise.
     */
    public boolean isModuleRegistered(String moduleName) {
        return modules.isModuleRegistered(moduleName);
    }

    /**
     * Add a signal declaration. This will generate a signal
     * declaration in the output code.
     *
     * @param signalName the signal name
     */
    public void addDeclaration(String signalName) {
        if (!declaredSignals.contains(signalName)) {
            VSignalDecl decl = registeredSignals.get(signalName);

            if (decl == null) {
                throw new RuntimeException("Declaration for '" + signalName + "' not found.");
            }
            decls.add(decl);
            declaredSignals.add(decl.getName());
        }
    }

    /**
     * Register a new signal declaration.
     *
     * @param name the signal name
     * @param bits the number of bits
     * @param type the type of the signal
     */
    public void registerSignalDecl(String name, int bits, VSignalDecl.Type type) {
        if (!registeredSignals.containsKey(name)) {
            VSignalDecl decl = new VSignalDecl(name, bits, type);
            registeredSignals.put(name, decl);
        }
    }

    /**
     * Register a new signal declaration.
     *
     * @param s the HDL signal
     * @param type the type of the signal
     */
    public void registerSignalDecl(Signal s, VSignalDecl.Type type) {
        registerSignalDecl(s.getName(), s.getBits(), type);
    }

    /**
     * Register and add a new signal declaration.
     *
     * @param s the HDL signal
     * @param type the type of the signal
     */
    public void registerAndAddSignalDecl(Signal s, VSignalDecl.Type type) {
        registerSignalDecl(s.getName(), s.getBits(), type);
        addDeclaration(s.getName());
    }

    /**
     * Return {@code VSignal} instance associated with a name
     *
     * @param name the signal name
     * @return the {@code VSignal} instance or null if no instance available
     */
    public VSignalDecl getSignalDecl(String name) {
        return registeredSignals.get(name);
    }

    /**
     * Returns the list of modules registered in the builder.
     *
     * @return the list of modules.
     */
    public ModuleList getModules() {
        return modules;
    }

    /**
     * Returns the list of statements registered in the builder.
     *
     * @return the list of statements.
     */
    public ArrayList<VStatement> getStatements() {
        return statements;
    }

    /**
     * Returns the list of initial statements
     *
     * @return the list of initial statements.
     */
    public ArrayList<VStatement> getInitialStatements() {
        return initialStatements;
    }

    /**
     * Returns the list of declarations registered in the builder.
     *
     * @return the list of declarations.
     */
    public ArrayList<VSignalDecl> getDeclarations() {
        return decls;
    }

    /**
     * Generates a new temporary name.
     *
     * @param baseName the base name to use.
     * @return the generated name
     */
    public String getNextName(String baseName) {
        if (!genNames.containsKey(baseName)) {
            genNames.put(baseName, 0);
        }

        int index = genNames.get(baseName);
        String name = baseName + index;
        index++;
        genNames.put(baseName, index);

        return name;
    }

    /**
     * Register the node output signals.
     *
     * @param node      the node.
     */
    public void registerNodeOutputs(HDLNode node) {
        for (Port p : node.getPorts().getOutputs()) {
            if (p.getSignal() != null) {
                outputSignalMap.put(p.getSignal(), node);
            }
        }
    }

    /**
     * Visit the node and builds an intermediate representation of the generated Verilog code if not
     * already visited.
     *
     * @param node the node to visit
     * @throws HDLException HDLException
     */
    public void visit(HDLNode node) throws HDLException {
        if (isNodeVisited(node)) {
            return;
        }

        visitInputNodes(node);

        // The node could has been visited by a recursive call
        if (!isNodeVisited(node)) {
            VerilogElement ve = vlibrary.getVerilogElement(node);

            ve.buildCodeIr(this, node);
            markNodeVisited(node);
        }
    }

    private void visitInputNodes(HDLNode node) throws HDLException {
        for (Port p : node.getPorts().getInputs()) {
            Signal s = p.getSignal();

            // Global inputs belong to the model and has no HDLNode associated.
            if (s.isPort()) {
                continue;
            }
            String signalName = s.getName();

            if (isSignalVisited(s)) {
                VIRNode n = getSignalCodeIr(s);

                // This can happen when there's a loop between signals
                if (n == null) {
                    VStatementPlace place = new VStatementPlace(s);
                    VStatement stmt = new VEmptyStatement(place);

                    VerilogCodeBuilder.this.setCodeIrForSignal(s, stmt);
                }
                continue;
            }
            markSignalVisited(s); // Avoid loops
            HDLNode providerNode = getHDLNode(s);

            if (providerNode == node) {
                setCodeIrForSignal(signalName, new VIdExpr(signalName));
            } else {
                visit(providerNode);
            }
        }
    }
}
