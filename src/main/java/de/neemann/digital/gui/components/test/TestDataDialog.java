package de.neemann.digital.gui.components.test;

import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Dialog to show and edit the test data source.
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
    public TestDataDialog(JComponent parent, TestData data) {
        super(SwingUtilities.getWindowAncestor(parent), Lang.get("key_Testdata"), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextArea text = new JTextArea(data.getDataString(), 30, 30);

        JScrollPane scrollPane = new JScrollPane(text);
        getContentPane().add(scrollPane);
        scrollPane.setRowHeaderView(new TextLineNumber(text, 3));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new ToolTipAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    data.setDataString(text.getText());
                    dispose();
                } catch (DataException e1) {
                    new ErrorMessage(e1.getMessage()).show(TestDataDialog.this);
                }
            }
        }.createJButton());

        getContentPane().add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }
}
