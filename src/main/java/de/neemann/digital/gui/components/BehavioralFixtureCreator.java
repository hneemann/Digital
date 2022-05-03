/*
 * Copyright (c) 2021 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.ModelModifier;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.parser.ParserException;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.Screen;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Dialog used to create a test case
 */
public class BehavioralFixtureCreator extends JDialog implements ModelModifier {
    private final JLabel frameLabel;
    private final Main main;
    private final ShapeFactory shapeFactory;
    private final StringBuilder testCase;
    private int testLines;
    private Model model;

    /**
     * Creates a new instance
     *
     * @param parent       the parent frame
     * @param shapeFactory the shapeFactory used to create the test case component
     */
    public BehavioralFixtureCreator(Main parent, ShapeFactory shapeFactory) {
        super(parent, Lang.get("menu_createBehavioralFixture"), false);
        main = parent;
        this.shapeFactory = shapeFactory;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frameLabel = new JLabel(Lang.get("msg_fixesCreated_N", testLines));
        frameLabel.setFont(Screen.getInstance().getFont(1.5f));
        frameLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(frameLabel);

        GridLayout layout = new GridLayout(2, 1);
        layout.setVgap(5);
        JPanel buttons = new JPanel(layout);
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        buttons.add(new ToolTipAction(Lang.get("btn_createTestLine")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                addFixture();
            }
        }.setToolTip(Lang.get("btn_createTestLine_tt")).createJButton());
        buttons.add(new ToolTipAction(Lang.get("btn_BehavioralFixtureComplete")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createTestCase();
            }
        }.setToolTip(Lang.get("btn_BehavioralFixtureComplete_tt")).createJButton());

        getContentPane().add(buttons, BorderLayout.SOUTH);

        testCase = new StringBuilder();

        pack();
        setLocation(parent.getLocation());
    }

    private void addFixture() {
        if (testCase.length() == 0) {

            for (Signal s : model.getInputs()) {
                if (testCase.length() > 0)
                    testCase.append(' ');
                testCase.append(s.getName());
            }
            for (Signal s : model.getOutputs())
                testCase.append(" ").append(s.getName());
            testCase.append('\n');
        }

        boolean first = true;
        for (Signal s : model.getInputs()) {
            if (first)
                first = false;
            else
                testCase.append(' ');
            addValue(testCase, s);
        }
        for (Signal s : model.getOutputs()) {
            testCase.append(' ');
            addValue(testCase, s);
        }
        testCase.append('\n');

        testLines++;
        frameLabel.setText(Lang.get("msg_fixesCreated_N", testLines));
    }

    private void addValue(StringBuilder testCase, Signal s) {
        ObservableValue value = s.getValue();
        if (value.isHighZ()) {
            testCase.append('Z');
        } else {
            int bits = value.getBits();

            if (bits <= 3)
                testCase.append(value.getValue());
            else
                testCase.append("0x").append(Long.toHexString(value.getValue()));
        }
    }

    private void createTestCase() {
        dispose();
        try {
            VisualElement tc = new VisualElement(TestCaseElement.DESCRIPTION.getName())
                    .setShapeFactory(shapeFactory)
                    .setAttribute(Keys.TESTDATA, new TestCaseDescription(testCase.toString()));
            SwingUtilities.invokeLater(() -> main.getCircuitComponent().setPartToInsert(tc));
        } catch (IOException | ParserException e) {
            SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorCreatingTestCase")).addCause(e));
        }
    }

    @Override
    public void preInit(Model model) throws NodeException {
        this.model = model;

        if (model.getInputs().isEmpty())
            throw new NodeException(Lang.get("err_analyseNoInputs"));
        if (model.getOutputs().isEmpty())
            throw new NodeException(Lang.get("err_analyseNoOutputs"));

        SwingUtilities.invokeLater(() -> setVisible(true));
    }

}
