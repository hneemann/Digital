/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.tt2;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.builder.*;
import de.neemann.digital.builder.jedec.FuseMapFillerException;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Exporter for the tt2 format needed by the ATF15xx fitters from ATMEL.
 */
public class TT2Exporter implements ExpressionExporter<TT2Exporter> {
    private final BuilderCollector builder;
    private final CleanNameBuilder cleanNameBuilder;
    private final PinMap pinMap;
    private String projectName;
    private String device;
    private OutputStreamWriter writer;
    private HashMap<String, Integer> varIndexMap;
    private HashMap<String, Integer> outIndexMap;
    private TreeMap<ProdInput, StateSet> termMap;
    private ArrayList<String> inputs;
    private ArrayList<String> outputs;
    private StateSet constants;
    private boolean constantsUsed = false;

    /**
     * Creates a new instance
     *
     * @param projectName project name
     */
    public TT2Exporter(String projectName) {
        // if simple aliases are filtered out, a direct input to output connection isn't possible anymore
        builder = new BuilderCollector();
        cleanNameBuilder = new CleanNameBuilder(builder);
        pinMap = cleanNameBuilder.createPinMap().setClockPin(43);
        device = "f1502ispplcc44";
        this.projectName = projectName;
    }

    @Override
    public BuilderInterface getBuilder() {
        return cleanNameBuilder;
    }

    /**
     * Sets the device name used
     *
     * @param device the device name
     * @return this for chained calls
     */
    public TT2Exporter setDevice(String device) {
        this.device = device;
        return this;
    }

    @Override
    public PinMap getPinMapping() {
        return pinMap;
    }

    @Override
    public void writeTo(OutputStream out) throws FuseMapFillerException, IOException, PinMapException {
        writeTo(new OutputStreamWriter(out, StandardCharsets.ISO_8859_1));
    }

    private void writeTo(OutputStreamWriter writer) throws IOException, FuseMapFillerException, PinMapException {
        createProductTerms();

        this.writer = writer;
        line("#$ TOOL CUPL");
        line("# Berkeley PLA format generated using Digital");
        line("#$ TITLE  " + projectName);
        line("#$ DEVICE  " + device);
        assignPinsAndNodes();
        //line("#$ PINS " + getPins());
        line(".i " + inputs.size());
        line(".o " + outputs.size());
        line(".type f");
        if (inputs.size() > 0)
            line(".ilb " + strList(inputs));
        line(".ob " + strList(outputs));
        line(".phase " + getPhase());

        line(".p " + termMap.size());
        for (Map.Entry<ProdInput, StateSet> e : termMap.entrySet())
            line(e.getKey() + " " + e.getValue());

        line(".e");
        writer.close();
    }

    private void createProductTerms() throws FuseMapFillerException {
        inputs = builder.getInputs();
        varIndexMap = new HashMap<>();
        int i = 0;
        for (String name : inputs) {
            checkName(name);
            varIndexMap.put(name, i);
            i++;
        }

        ProdInput clkIn = null;
        if (!builder.getRegistered().isEmpty()) {
            int clk = inputs.size();
            inputs.add("CLK");
            varIndexMap.put("CLK", i);
            i++;
            for (String reg : builder.getRegistered().keySet()) {
                inputs.add(reg + ".Q");
                varIndexMap.put(reg, i);
                i++;
            }

            clkIn = new ProdInput(inputs.size());
            clkIn.set(clk, 1);
        }

        ArrayList<Integer> clkInList = new ArrayList<>();
        outputs = new ArrayList<>();
        outIndexMap = new HashMap<>();
        i = 0;
        for (String name : builder.getOutputs()) {
            checkName(name);
            if (builder.getRegistered().containsKey(name)) {
                outIndexMap.put(name + ".REG", i++);
                outputs.add(name + ".REG");
                outIndexMap.put(name + ".AR", i++);
                outputs.add(name + ".AR");
                clkInList.add(i);
                outIndexMap.put(name + ".C", i++);
                outputs.add(name + ".C");
            } else {
                outIndexMap.put(name, i++);
                outputs.add(name);
            }
        }

        termMap = new TreeMap<>();
        if (!builder.getRegistered().isEmpty()) { // connect clock and ar
            StateSet clk = new StateSet(outputs.size());
            for (int c : clkInList)
                clk.set(c, 1);
            termMap.put(clkIn, clk);
            constantsUsed = true;
        }

        constants = new StateSet(outputs.size());
        final ProdInput constProdInput = new ProdInput(inputs.size());
        termMap.put(constProdInput, constants);

        for (Map.Entry<String, Expression> e : builder.getCombinatorial().entrySet())
            addExpression(e.getKey(), e.getValue());

        for (Map.Entry<String, Expression> e : builder.getRegistered().entrySet())
            addExpression(e.getKey() + ".REG", e.getValue());

        if (!constantsUsed)
            termMap.remove(constProdInput);
    }

    static void checkName(String name) throws FuseMapFillerException {
        if (name.length() == 0)
            throw new FuseMapFillerException(Lang.get("err_invalidPinName_N", name));

        char first = name.charAt(0);
        if (first >= '0' && first <= '9')
            throw new FuseMapFillerException(Lang.get("err_invalidPinName_N", name));

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(c >= '0' && c <= '9'
                    || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c == '_'))) {
                throw new FuseMapFillerException(Lang.get("err_invalidPinName_N", name));
            }
        }
    }

    private void addExpression(String name, Expression expression) throws FuseMapFillerException {
        if (expression instanceof Operation.Or) {
            Operation.Or or = (Operation.Or) expression;
            for (Expression e : or.getExpressions())
                addProdFor(name, e);
        } else if (expression instanceof Constant) {
            constantsUsed = true;
            if (expression == Constant.ONE)
                constants.set(getOutNum(name));
        } else
            addProdFor(name, expression);
    }

    private void addProdFor(String name, Expression e) throws FuseMapFillerException {
        ProdInput pt = new ProdInput(getInputCount());
        if (e instanceof Operation.And) {
            Operation.And and = (Operation.And) e;
            for (Expression z : and.getExpressions())
                pt.add(z);
        } else
            pt.add(e);

        StateSet o = termMap.computeIfAbsent(pt, k -> new StateSet(getOutputCount()));
        o.set(getOutNum(name));
    }

    private String strList(ArrayList<String> pins) {
        StringBuilder sb = new StringBuilder();
        for (String p : pins) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(p);
        }
        return sb.toString();
    }

    private void line(String s) throws IOException {
        writer.write(s);
        writer.write("\r\n");
    }

    private int getInputCount() {
        return varIndexMap.size();
    }

    private int getVarNum(String identifier) throws FuseMapFillerException {
        final Integer i = varIndexMap.get(identifier);
        if (i == null)
            throw new FuseMapFillerException("ident " + identifier + " not found!");
        return i;
    }

    private int getOutputCount() {
        return outIndexMap.size();
    }

    private int getOutNum(String identifier) {
        return outIndexMap.get(identifier);
    }

    private String getPhase() {
        StringBuilder sb = new StringBuilder(getOutputCount());
        for (int i = 0; i < getOutputCount(); i++)
            sb.append("1");
        return sb.toString();
    }

    private void assignPinsAndNodes() throws IOException, PinMapException {
        int pinNum = 0;
        StringBuilder pin = new StringBuilder();
        int nodeNum = 0;
        StringBuilder node = new StringBuilder();

        for (String i : builder.getInputs()) {
            int p = pinMap.getInputFor(i);
            pin.append(" ").append(i).append("+:").append(p);
            pinNum++;
        }

        if (!builder.getRegistered().isEmpty()) {
            pin.append(" CLK+:").append(pinMap.getClockPin());
            pinNum++;
        }

        for (String o : builder.getOutputs()) {
            int p = pinMap.isOutputAssigned(o);
            if (p >= 0) {
                pin.append(" ").append(o).append("+:").append(p);
                pinNum++;
            } else {
                node.append(" ").append(o);
                nodeNum++;
            }
        }

        if (pinNum > 0)
            line("#$ PINS " + pinNum + pin.toString());
        if (nodeNum > 0)
            line("#$ NODES " + nodeNum + node.toString());
    }

    //StateSet can not be final because its overridden. Maybe checkstyle has a bug?
    //CHECKSTYLE.OFF: FinalClass
    private static class StateSet implements Comparable<StateSet> {
        private final int[] state;

        private StateSet(int outputCount) {
            state = new int[outputCount];
        }

        void setAllToUnused() {
            Arrays.fill(state, 2);
        }

        private void set(int i) {
            set(i, 1);
        }

        void set(int i, int value) {
            state[i] = value;
        }

        @Override
        public int compareTo(StateSet stateSet) {
            for (int i = 0; i < state.length; i++) {
                int c = Integer.compare(state[i], stateSet.state[i]);
                if (c != 0)
                    return c;
            }
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StateSet stateSet = (StateSet) o;

            return Arrays.equals(state, stateSet.state);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(state);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(state.length);
            for (int i : state)
                switch (i) {
                    case 0:
                        sb.append("0");
                        break;
                    case 1:
                        sb.append("1");
                        break;
                    default:
                        sb.append("-");
                }
            return sb.toString();
        }
    }
    //CHECKSTYLE.ON: FinalClass

    private final class ProdInput extends StateSet {
        private ProdInput(int inputCount) {
            super(inputCount);
            setAllToUnused();
        }

        public void add(Expression z) throws FuseMapFillerException {
            if (z instanceof Not)
                add(((Not) z).getExpression(), true);
            else
                add(z, false);
        }

        private void add(Expression var, boolean invers) throws FuseMapFillerException {
            if (var instanceof Variable) {
                set(getVarNum(((Variable) var).getIdentifier()), invers ? 0 : 1);
            } else if (var instanceof Constant) {
                throw new FuseMapFillerException(Lang.get("err_constantsNotAllowed"));
            } else
                throw new FuseMapFillerException("invalid expression");
        }
    }

}
