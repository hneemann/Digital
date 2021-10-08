/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.format;

import de.neemann.digital.analyse.ModelAnalyserInfo;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Creates a test case which represents the truth table
 */
public class TruthTableFormatterTestCase implements TruthTableFormatter {
    private enum Type {NORMAL, FIRSTBIN, BIN}

    private final ArrayList<ModelAnalyserInfo.Bus> inputBusList;
    private final ArrayList<ModelAnalyserInfo.Bus> outputBusList;

    /**
     * Creates a new instance.
     *
     * @param modelAnalyzerInfo the information about the usage of multi bit signals
     */
    public TruthTableFormatterTestCase(ModelAnalyserInfo modelAnalyzerInfo) {
        if (modelAnalyzerInfo == null) {
            inputBusList = new ArrayList<>();
            outputBusList = new ArrayList<>();
        } else {
            inputBusList = modelAnalyzerInfo.getInputBusList();
            outputBusList = modelAnalyzerInfo.getOutputBusList();
        }
    }

    @Override
    public String format(TruthTable truthTable) throws ExpressionException {
        StringBuilder sb = new StringBuilder();

        ArrayList<String> inputs = new ArrayList<>();
        for (Variable v : truthTable.getVars())
            inputs.add(v.getIdentifier());
        ArrayList<Type> inputOutType = outVars(sb, inputs, inputBusList);

        ArrayList<String> outputs = new ArrayList<>();
        for (int i = 0; i < truthTable.getResultCount(); i++)
            outputs.add(truthTable.getResultName(i));
        ArrayList<Type> outputOutType = outVars(sb, outputs, outputBusList);

        sb.append("\n\n");

        ContextFiller cf = new ContextFiller(truthTable.getVars());
        for (int i = 0; i < cf.getRowCount(); i++) {
            cf.setContextTo(i);
            int ind = 0;
            for (Variable v : cf)
                outValue(sb, inputOutType.get(ind++), ThreeStateValue.value(cf.get(v)));

            ind = 0;
            for (int j = 0; j < truthTable.getResultCount(); j++)
                outValue(sb, outputOutType.get(ind++), truthTable.getResult(j).get(i));
            sb.append("\n");
        }
        return sb.toString();
    }

    private void outValue(StringBuilder sb, Type type, ThreeStateValue b) {
        switch (type) {
            case NORMAL:
                sb.append(" ").append(formatValue(b));
                break;
            case FIRSTBIN:
                sb.append(" 0b").append(formatValue(b));
                break;
            case BIN:
                sb.append(formatValue(b));
                break;
        }

    }

    private ArrayList<Type> outVars(StringBuilder sb, ArrayList<String> vars, ArrayList<ModelAnalyserInfo.Bus> busList) {
        ArrayList<Type> types = new ArrayList<>(vars.size());
        HashMap<String, String> map = new HashMap<>();
        for (ModelAnalyserInfo.Bus b : busList) {
            String last = null;
            for (String s : b.getSignalNames()) {
                map.put(s, "");
                last = s;
            }
            map.put(last, b.getBusName());
        }
        for (String n : vars) {
            String r = map.get(n);
            if (r == null) {
                sb.append(n).append(" ");
                types.add(Type.NORMAL);
            } else if (!r.isEmpty()) {
                sb.append(r).append(" ");
                types.add(Type.FIRSTBIN);
            } else
                types.add(Type.BIN);
        }
        return types;
    }

    private String formatValue(ThreeStateValue r) {
        switch (r) {
            case one:
                return "1";
            case zero:
                return "0";
            case dontCare:
                return "x";
        }
        return null;
    }

}
