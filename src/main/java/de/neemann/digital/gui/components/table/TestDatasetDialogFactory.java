/*
 * Copyright (c) 2021 Anatolii Titov.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.ModelAnalyserInfo;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.Signal;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        ArrayList<Signal> signals = Stream
                .concat(model.getInputs().stream(), model.getOutputs().stream())
                .collect(Collectors.toCollection(ArrayList::new));

        StringBuilder sb = new StringBuilder();

        for (Signal s : signals) {
            sb
                    .append(s.getName())
                    .append(" ");
        }

        sb.append("\n\n");

        for (Signal s : signals) {
            String value;
            if (s.getValue().getBits() == 1) {
                value = Long.toBinaryString(s.getValue().getValue());
            } else {
                value = formatBitFixedSize(s.getValue().getValue(), s.getValue().getBits());
            }

            sb
                    .append(value)
                    .append(" ");
        }

        return sb.toString();
    }

    private String formatBitFixedSize(long number, long size) {
        long mask = 1L << size;
        return "0b" + Long.toBinaryString(mask | number).substring(1);
    }

}
