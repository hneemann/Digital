package de.neemann.digital.builder.tt2;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Not;
import de.neemann.digital.analyse.expression.Operation;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.builder.*;
import de.neemann.digital.builder.jedec.FuseMapFillerException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Exporter for the tt2 format needed by the ATF15xx fitters from ATMEL.
 * Created by hneemann on 03.03.17.
 */
public class TT2Exporter implements ExpressionExporter<TT2Exporter> {
    private final BuilderCollector builder;
    private final PinMap pinMap;
    private int clockPin;
    private String projectName;
    private String device;
    private OutputStreamWriter writer;
    private HashMap<String, Integer> varIndexMap;
    private HashMap<String, Integer> outIndexMap;
    private TreeMap<ProdInput, StateSet> termMap;
    private ArrayList<String> inputs;
    private ArrayList<String> outputs;

    /**
     * Creates a new instance
     */
    public TT2Exporter() {
        builder = new BuilderCollector() {
            @Override
            public BuilderCollector addCombinatorial(String name, Expression expression) throws BuilderException {
                if (pinMap.isSimpleAlias(name, expression))
                    return this;
                else
                    return super.addCombinatorial(name, expression);
            }
        };
        pinMap = new PinMap();
        device = "f1502ispplcc44";
        projectName = "unknown";
        clockPin = 43;
    }

    @Override
    public BuilderInterface getBuilder() {
        return builder;
    }

    /**
     * Sets the pin connected to the clock of the ff
     *
     * @param clockPin the pin number
     * @return this for chained calls
     */
    public TT2Exporter setClockPin(int clockPin) {
        this.clockPin = clockPin;
        return this;
    }

    /**
     * Sets the project name used
     *
     * @param projectName the project name
     * @return this for chained calls
     */
    public TT2Exporter setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
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
        writeTo(new OutputStreamWriter(out, "ISO-8859-1"));
    }

    private void writeTo(OutputStreamWriter writer) throws IOException, FuseMapFillerException, PinMapException {
        createProductTerms();

        this.writer = writer;
        line("#$ TOOL CUPL");
        line("# Berkeley PLA format generated using Digital");
        line("#$ TITLE  " + projectName);
        line("#$ MODULE  " + projectName);
        line("#$ JEDECFILE  " + projectName);
        line("#$ DEVICE  " + device);
        line("#$ PINS " + getPins());
        line(".i " + inputs.size());
        line(".o " + outputs.size());
        line(".type f");
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

            // AR
            termMap.put(new ProdInput(inputs.size()), new StateSet(outputs.size()));
        }


        for (Map.Entry<String, Expression> e : builder.getCombinatorial().entrySet())
            addExpression(e.getKey(), e.getValue());

        for (Map.Entry<String, Expression> e : builder.getRegistered().entrySet())
            addExpression(e.getKey() + ".REG", e.getValue());
    }

    private void addExpression(String name, Expression expression) throws FuseMapFillerException {
        if (expression instanceof Operation.Or) {
            Operation.Or or = (Operation.Or) expression;
            for (Expression e : or.getExpressions())
                addProdFor(name, e);
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

    private String getPins() throws PinMapException {
        StringBuilder sb = new StringBuilder();
        int numPins = builder.getInputs().size() + builder.getOutputs().size();
        if (!builder.getRegistered().isEmpty())
            numPins++;

        sb.append(numPins);


        for (String i : builder.getInputs()) {
            int p = pinMap.getInputFor(i);
            sb.append(" ").append(i).append("+:").append(p);
        }

        if (!builder.getRegistered().isEmpty())
            sb.append(" CLK+:").append(clockPin);

        for (String o : builder.getOutputs()) {
            int p = pinMap.getInputFor(o);
            sb.append(" ").append(o).append("+:").append(p);
        }


        return sb.toString();
    }

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
            } else
                throw new FuseMapFillerException("invalid expression");
        }
    }

}
