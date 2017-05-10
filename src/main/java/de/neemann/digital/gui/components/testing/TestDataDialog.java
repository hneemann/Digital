package de.neemann.digital.gui.components.testing;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.table.ShowStringDialog;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestData;
import de.neemann.digital.testing.Transitions;
import de.neemann.digital.testing.parser.ParserException;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.Screen;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Dialog to show and edit the testing data source.
 *
 * @author hneemann
 */
public class TestDataDialog extends JDialog {

    /**
     * Creates a new data dialog
     *
     * @param parent the parent component
     * @param data   the data to edit
     */
    public TestDataDialog(Component parent, TestData data) {
        this(parent, data, null, null);
    }


    /**
     * Creates a new data dialog
     *
     * @param parent            the parent component
     * @param data              the data to edit
     * @param key               the key for the apply button
     * @param elementAttributes the attributes to store the values
     */
    public TestDataDialog(Component parent, TestData data, Key<TestData> key, ElementAttributes elementAttributes) {
        super(SwingUtilities.getWindowAncestor(parent),
                Lang.get("key_Testdata"),
                key == null ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextArea text = new JTextArea(data.getDataString(), 30, 50);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Screen.getInstance().getFontSize()));

        JScrollPane scrollPane = new JScrollPane(text);
        getContentPane().add(scrollPane);
        scrollPane.setRowHeaderView(new TextLineNumber(text, 3));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        buttons.add(new ToolTipAction(Lang.get("btn_help")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ShowStringDialog(
                        TestDataDialog.this,
                        Lang.get("msg_testVectorHelpTitle"),
                        Lang.get("msg_testVectorHelp"), true)
                        .setVisible(true);
            }
        }.createJButton());

        if (Main.enableExperimental()) {
            buttons.add(new ToolTipAction(Lang.get("btn_addTransitions")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parent instanceof CircuitComponent) {
                        CircuitComponent cc = (CircuitComponent) parent;
                        try {
                            Transitions tr = new Transitions(text.getText(), cc.getCircuit().getInputNames());
                            if (tr.isNew()) {
                                text.setText(tr.getCompletedText());
                            }
                        } catch (ParserException | IOException | PinException e1) {
                            new ErrorMessage(e1.getMessage()).show(TestDataDialog.this);
                        }
                    }
                }
            }.setToolTip(Lang.get("btn_addTransitions_tt")).createJButton());
        }

        if (key != null && elementAttributes != null) {
            buttons.add(new ToolTipAction(Lang.get("menu_runTests")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        data.setDataString(text.getText());
                        elementAttributes.set(key, data);
                        if (parent instanceof CircuitComponent) {
                            CircuitComponent cc = (CircuitComponent) parent;
                            cc.getCircuit().modified();
                            cc.getMain().startTests();
                        }
                    } catch (ParserException | IOException e1) {
                        new ErrorMessage(e1.getMessage()).show(TestDataDialog.this);
                    }
                }
            }.createJButton());
        }

        buttons.add(new ToolTipAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    data.setDataString(text.getText());
                    if (key != null && elementAttributes != null) {
                        elementAttributes.set(key, data);
                        if (parent instanceof CircuitComponent) {
                            CircuitComponent cc = (CircuitComponent) parent;
                            cc.getCircuit().modified();
                        }
                    }
                    dispose();
                } catch (ParserException | IOException e1) {
                    new ErrorMessage(e1.getMessage()).show(TestDataDialog.this);
                }
            }
        }.createJButton());


        getContentPane().add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

}
