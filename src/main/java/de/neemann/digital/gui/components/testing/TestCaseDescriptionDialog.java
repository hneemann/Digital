/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.testing;

import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.TextLineNumber;
import de.neemann.digital.gui.components.modification.ModifyAttribute;
import de.neemann.digital.gui.components.table.ShowStringDialog;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.Transitions;
import de.neemann.digital.testing.parser.ParserException;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.Screen;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

import static de.neemann.digital.gui.components.EditorFactory.addF1Traversal;

/**
 * Dialog to show and edit the testing data source.
 */
public class TestCaseDescriptionDialog extends JDialog {

    /**
     * Creates a new data dialog
     *
     * @param parent  the parent component
     * @param data    the data to edit
     * @param element the element to be modified
     */
    public TestCaseDescriptionDialog(Window parent, TestCaseDescription data, VisualElement element) {
        super(parent,
                Lang.get("key_Testdata"),
                element == null ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);


        String initialDataString = data.getDataString();

        JTextArea text = addF1Traversal(new JTextArea(data.getDataString(), 30, 50));
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Screen.getInstance().getFontSize()));

        JScrollPane scrollPane = new JScrollPane(text);
        getContentPane().add(scrollPane);
        scrollPane.setRowHeaderView(new TextLineNumber(text, 3));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        buttons.add(new ToolTipAction(Lang.get("btn_help")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ShowStringDialog(
                        TestCaseDescriptionDialog.this,
                        Lang.get("msg_testVectorHelpTitle"),
                        Lang.get("msg_testVectorHelp"), true)
                        .setVisible(true);
            }
        }.createJButton());

        if (Main.isExperimentalMode()) {
            buttons.add(new ToolTipAction(Lang.get("btn_addTransitions")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parent instanceof Main) {
                        CircuitComponent cc = ((Main) parent).getCircuitComponent();
                        try {
                            Transitions tr = new Transitions(text.getText(), cc.getCircuit().getInputNames());
                            if (tr.isNew()) {
                                text.setText(tr.getCompletedText());
                            }
                        } catch (ParserException | IOException | PinException e1) {
                            new ErrorMessage(e1.getMessage()).show(TestCaseDescriptionDialog.this);
                        }
                    }
                }
            }.setToolTip(Lang.get("btn_addTransitions_tt")).createJButton());
        }

        buttons.add(new ToolTipAction(Lang.get("cancel")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        }.createJButton());

        if (element != null) {
            buttons.add(new ToolTipAction(Lang.get("menu_runTests")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        data.setDataString(text.getText());
                        if (parent instanceof Main) {
                            CircuitComponent cc = ((Main) parent).getCircuitComponent();
                            element.getElementAttributes().set(TestCaseElement.TESTDATA, data);
                            cc.getMain().startTests();
                        }
                    } catch (ParserException | IOException e1) {
                        new ErrorMessage(e1.getMessage()).show(TestCaseDescriptionDialog.this);
                    }
                }
            }.createJButton());
        }

        buttons.add(new ToolTipAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    data.setDataString(text.getText());
                    if (element != null
                            && !initialDataString.equals(data.getDataString())
                            && parent instanceof Main) {
                        CircuitComponent cc = ((Main) parent).getCircuitComponent();
                        cc.modify(new ModifyAttribute<>(element, TestCaseElement.TESTDATA, new TestCaseDescription(data)));
                    }
                    dispose();
                } catch (ParserException | IOException e1) {
                    new ErrorMessage(e1.getMessage()).show(TestCaseDescriptionDialog.this);
                }
            }
        }.createJButton());


        getContentPane().add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

}
