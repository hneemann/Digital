/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.testing;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.TextLineNumber;
import de.neemann.digital.gui.components.modification.ModifyAttribute;
import de.neemann.digital.gui.components.table.ShowStringDialog;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.Transitions;
import de.neemann.digital.testing.parser.ParserException;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.Screen;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import static de.neemann.digital.gui.components.EditorFactory.addF1Traversal;
import static de.neemann.digital.gui.components.EditorFactory.createUndoManager;

/**
 * Dialog to show and edit the testing data source.
 */
public class TestCaseDescriptionDialog extends JDialog {

    private final JTextArea text;
    private final TestCaseDescription initialData;
    private final VisualElement element;
    private TestCaseDescription modifiedData;
    private boolean circuitModified = false;

    /**
     * Creates a new data dialog.
     *
     * @param parent      the parent component
     * @param initialData the data to edit
     */
    public TestCaseDescriptionDialog(Window parent, TestCaseDescription initialData) {
        this(parent, initialData, null);
    }

    /**
     * Creates a new data dialog.
     * This constructor allows to open the dialog in a modeless way.
     *
     * @param parent      the parent component
     * @param initialData the data to edit
     * @param element     the element to be modified
     */
    public TestCaseDescriptionDialog(Window parent, TestCaseDescription initialData, VisualElement element) {
        super(parent,
                Lang.get("key_Testdata"),
                element == null ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
        this.element = element;
        this.initialData = initialData;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        text = addF1Traversal(new JTextArea(initialData.getDataString(), 30, 50));
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Screen.getInstance().getFontSize()));

        createUndoManager(text);

        addWindowListener(new ClosingWindowListener());

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
                tryDispose();
            }
        }.createJButton());

        if (element != null) {
            buttons.add(new ToolTipAction(Lang.get("menu_runTests")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (parent instanceof Main) {
                            CircuitComponent cc = ((Main) parent).getCircuitComponent();
                            element.getElementAttributes().set(Keys.TESTDATA, new TestCaseDescription(text.getText()));
                            circuitModified = true;
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
                    modifiedData = new TestCaseDescription(text.getText());
                    if (element != null
                            && isStateChanged()
                            && parent instanceof Main) {
                        CircuitComponent cc = ((Main) parent).getCircuitComponent();
                        cc.modify(new ModifyAttribute<>(element, Keys.TESTDATA, modifiedData));
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

    /**
     * Shows the dialog and returns the modified data
     *
     * @return the modified data or null if not modified
     */
    public TestCaseDescription showDialog() {
        modifiedData = null;
        setVisible(true);
        return modifiedData;
    }

    private boolean isStateChanged() {
        return !initialData.getDataString().equals(text.getText());
    }

    private void tryDispose() {
        if (isStateChanged()) {
            int r = JOptionPane.showOptionDialog(
                    this,
                    Lang.get("msg_dataWillBeLost_n", Keys.TESTDATA.getName()),
                    Lang.get("msg_warning"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, new String[]{Lang.get("btn_discard"), Lang.get("btn_editFurther")},
                    Lang.get("cancel"));
            if (r == JOptionPane.YES_OPTION)
                myDispose();
        } else
            myDispose();
    }

    private void myDispose() {
        dispose();
        if (circuitModified)
            element.getElementAttributes().set(Keys.TESTDATA, initialData);
    }

    private final class ClosingWindowListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            tryDispose();
        }
    }

}
