package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.ModelAnalyserInfo;
import de.neemann.digital.analyse.expression.ContextFiller;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.Signal;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Creates a String Dialog with current model input and output values
 */
public final class TestDatasetDialogFactory {
    private enum Type {NORMAL, FIRSTBIN, BIN}

    private Model model;
    private JFrame parent;

    private ModelAnalyserInfo modelAnalyserInfo;


    /**
     * Constructor
     *
     * @param parent Parent frame
     * @param model  Current model
     */
    public TestDatasetDialogFactory(JFrame parent, Model model) {
        this.model = model;
        this.parent = parent;

        this.modelAnalyserInfo = new ModelAnalyserInfo(model);
    }


    /**
     * Dialog factory
     *
     * @return Dialog
     */
    public JDialog create() {
        String text;

        try {
            text = format();
        } catch (ExpressionException e) {
            text = "Exception!\n" + e.getMessage();
        }

        ShowStringDialog dialog = new ShowStringDialog(
                parent,
                Lang.get("win_testDatasetDialog"),
                text
        );

        dialog.setVisible(true);
        return dialog;
    }


    private String format() throws ExpressionException {
        HashMap<String, ArrayList<String>> inputBusMap = modelAnalyserInfo.getInputBusMap();
        HashMap<String, ArrayList<String>> outputBusMap = modelAnalyserInfo.getOutputBusMap();
        ArrayList<Signal> modelInputs = model.getInputs();

        StringBuilder sb = new StringBuilder();

        ArrayList<String> inputs = new ArrayList<>();
        for (Signal s : modelInputs)
            inputs.add(s.getName());
        ArrayList<Type> inputOutType = outVars(sb, inputs, inputBusMap);

        ArrayList<String> outputs = new ArrayList<>();
        for (Signal s : model.getOutputs())
            outputs.add(s.getName());
        ArrayList<Type> outputOutType = outVars(sb, outputs, outputBusMap);

        sb.append("\n\n");

        ContextFiller cf = new ContextFiller(
                modelInputs.stream()
                        .map(signal -> new Variable(signal.getName()))
                        .collect(Collectors.toList())
        );
        for (int i = 0; i < cf.getRowCount(); i++) {
            cf.setContextTo(i);
            int ind = 0;
            for (Variable v : cf) {
                outValue(sb, inputOutType.get(ind++), ThreeStateValue.value(cf.get(v)));
            }

            ind = 0;
            for (Signal s : model.getOutputs()) {
                outValue(sb, outputOutType.get(ind++), ThreeStateValue.value(s.getValue().getBool()));
            }
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

    private ArrayList<Type> outVars(StringBuilder sb, ArrayList<String> vars, HashMap<String, ArrayList<String>> busMap) {
        ArrayList<Type> types = new ArrayList<>(vars.size());
        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<String, ArrayList<String>> e : busMap.entrySet()) {
            String last = null;
            for (String s : e.getValue()) {
                map.put(s, "");
                last = s;
            }
            map.put(last, e.getKey());
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
