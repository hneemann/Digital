/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.expression;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.parser.Parser;
import de.neemann.digital.builder.circuit.CircuitBuilder;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.table.ShowStringDialog;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import static de.neemann.digital.gui.components.EditorFactory.addF1Traversal;

/**
 * Dialog to enter an expression.
 * Creates a new frame with a circuit generated from the entered expression.
 */
public class ExpressionDialog extends JDialog {

    /**
     * Creates a new instance
     *
     * @param parent       the parent
     * @param library      the library to use
     * @param shapeFactory the shapeFactory used for new circuits
     * @param baseFilename filename used as base for file operations
     */
    public ExpressionDialog(Main parent, ElementLibrary library, ShapeFactory shapeFactory, File baseFilename) {
        super(parent, Lang.get("expression"), false);

        String exampleEquation = "(C ∨ B) ∧ (A ∨ C) ∧ (B ∨ !C) * (C + !A)";
        JTextArea text = addF1Traversal(new JTextArea(exampleEquation, 5, 40));
        getContentPane().add(new JScrollPane(text), BorderLayout.CENTER);
        getContentPane().add(new JLabel(Lang.get("msg_enterAnExpression")), BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttons, BorderLayout.SOUTH);

        buttons.add(new ToolTipAction(Lang.get("btn_help")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ShowStringDialog(
                        ExpressionDialog.this,
                        Lang.get("msg_expressionHelpTitle"),
                        Lang.get("msg_expressionHelp"))
                        .setVisible(true);
            }
        }.createJButton());

        buttons.add(new ToolTipAction(Lang.get("btn_create")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ArrayList<Expression> expList = new Parser(text.getText()).parse();
                    CircuitBuilder circuitBuilder = new CircuitBuilder(shapeFactory).setResolveLocalVars(true);
                    if (expList.size() == 1)
                        circuitBuilder.addCombinatorial("Y", expList.get(0));
                    else
                        for (Expression exp : expList)
                            circuitBuilder.addCombinatorial(FormatToExpression.defaultFormat(exp), exp);
                    Circuit circuit = circuitBuilder.createCircuit();
                    new Main.MainBuilder()
                            .setParent(parent)
                            .setLibrary(library)
                            .setCircuit(circuit)
                            .setBaseFileName(baseFilename)
                            .openLater();
                } catch (Exception ex) {
                    new ErrorMessage().addCause(ex).show(ExpressionDialog.this);
                }
            }
        }.setToolTip(Lang.get("btn_create_tt")).createJButton());

        pack();
        setLocationRelativeTo(parent);
    }
}
