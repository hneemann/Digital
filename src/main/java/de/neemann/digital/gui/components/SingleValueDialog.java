package de.neemann.digital.gui.components;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.gui.sync.Sync;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * Dialog to edit a single value.
 * Used to enter a multi bit input value.
 *
 * @author hneemann
 * @author Rüdiger Heintz
 */
public final class SingleValueDialog extends JDialog {

    private enum InMode {
        HEX(Lang.get("attr_dialogHex")),
        DECIMAL(Lang.get("attr_dialogDecimal")),
        OCTAL(Lang.get("attr_dialogOctal")),
        ASCII(Lang.get("attr_dialogAscii")),
        // highZ needs to be the last entry!! See InMode#values(boolean)
        HIGHZ(Lang.get("attr_dialogHighz"));

        private String langText;

        InMode(String langKey) {
            this.langText = langKey;
        }

        @Override
        public String toString() {
            return langText;
        }

        public static InMode[] values(boolean supportsHighZ) {
            if (supportsHighZ) {
                return values();
            } else {
                return Arrays.copyOf(values(), values().length - 1);
            }
        }
    }

    private final JTextField textField;
    private final boolean supportsHighZ;
    private final JComboBox<InMode> formatComboBox;
    private JCheckBox[] checkBoxes;
    private boolean programmaticModifyingFormat = false;
    private long editValue;
    private boolean ok = false;

    private SingleValueDialog(Point pos, ObservableValue value) {
        super((Frame) null, Lang.get("attr_dialogTitle"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        editValue = value.getValue();
        supportsHighZ = value.supportsHighZ();

        textField = new JTextField(10);
        textField.setHorizontalAlignment(JTextField.RIGHT);
        formatComboBox = new JComboBox<>(InMode.values(supportsHighZ));
        formatComboBox.addActionListener(actionEvent -> {
            if (!programmaticModifyingFormat)
                setLongToDialog(editValue);
        });

        JPanel panel = new JPanel(new DialogLayout());
        panel.add(formatComboBox, DialogLayout.LABEL);
        panel.add(textField, DialogLayout.INPUT);
        panel.add(new JLabel(Lang.get("attr_dialogBinary")), DialogLayout.LABEL);
        panel.add(createCheckBoxPanel(value.getBits(), editValue), DialogLayout.INPUT);
        getContentPane().add(panel);

        textField.getDocument().addDocumentListener(new MyDocumentListener(() -> setStringToDialog(textField.getText())));

        if (value.isHighZ())
            formatComboBox.setSelectedItem(InMode.HIGHZ);
        else
            setLongToDialog(editValue);

        JButton okButton = new JButton(new AbstractAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ok = true;
                dispose();
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        getContentPane().add(buttonPanel, BorderLayout.EAST);

        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(actionEvent -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocation(pos.x, pos.y);
        textField.requestFocus();
        textField.select(0, Integer.MAX_VALUE);
        setAlwaysOnTop(true);
    }

    private JPanel createCheckBoxPanel(int bits, long value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        checkBoxes = new JCheckBox[bits];
        for (int i = bits - 1; i >= 0; i--) {
            final int bit = i;
            checkBoxes[bit] = new JCheckBox("", (value & (1L << bit)) != 0);
            checkBoxes[bit].setBorder(null);
            checkBoxes[bit].addActionListener(actionEvent -> setBit(bit, checkBoxes[bit].isSelected()));
            p.add(checkBoxes[bit]);
        }
        return p;
    }

    private void setBit(int bitNum, boolean set) {
        if (set)
            editValue |= 1L << bitNum;
        else
            editValue &= ~(1L << bitNum);

        if (getSelectedFormat().equals(InMode.HIGHZ))
            setSelectedFormat(InMode.HEX);

        setLongToDialog(editValue);
    }

    private void setLongToDialog(long editValue) {
        switch (getSelectedFormat()) {
            case ASCII:
                char val = (char) (editValue);
                textField.setText("\'" + val + "\'");
                textField.setCaretPosition(1);
                break;
            case DECIMAL:
                textField.setText(Long.toString(editValue));
                break;
            case HEX:
                textField.setText("0x" + Long.toHexString(editValue));
                break;
            case OCTAL:
                textField.setText("0" + Long.toOctalString(editValue));
                break;
            case HIGHZ:
                textField.setText("?");
                break;
            default:
        }
        textField.requestFocus();
    }

    private InMode getSelectedFormat() {
        return (InMode) formatComboBox.getSelectedItem();
    }

    private void setSelectedFormat(InMode format) {
        if (!getSelectedFormat().equals(format)) {
            programmaticModifyingFormat = true;
            formatComboBox.setSelectedItem(format);
            programmaticModifyingFormat = false;
        }
    }

    private void setStringToDialog(String text) {
        text = text.trim();
        if (text.length() > 0) {
            if (text.contains("?") && supportsHighZ) {
                setSelectedFormat(InMode.HIGHZ);
                editValue = 0;
            } else if (text.charAt(0) == '\'') {
                setSelectedFormat(InMode.ASCII);
                if (text.length() > 1) {
                    editValue = text.charAt(1);
                } else {
                    editValue = 0;
                }
            } else {
                if (text.startsWith("0x"))
                    setSelectedFormat(InMode.HEX);
                else if (text.startsWith("0"))
                    setSelectedFormat(InMode.OCTAL);
                else
                    setSelectedFormat(InMode.DECIMAL);
                try {
                    editValue = Long.decode(text);
                } catch (NumberFormatException e) {
                    // do nothing on error
                }
            }
            for (int i = 0; i < checkBoxes.length; i++)
                checkBoxes[i].setSelected((editValue & (1L << i)) != 0);
        }
    }

    private boolean showDialog() {
        setVisible(true);
        return ok;
    }

    /**
     * Edits a single value
     *
     * @param pos   the position to pop up the dialog
     * @param value the value to edit
     */
    public static void editValue(Point pos, ObservableValue value, Sync modelSync) {
        SingleValueDialog svd = new SingleValueDialog(pos, value);
        if (svd.showDialog()) {
            if (svd.getSelectedFormat().equals(InMode.HIGHZ)) {
                modelSync.access(() -> value.setHighZ(true));
            } else {
                modelSync.access(() -> value.set(svd.editValue, false));
            }
        }
    }


    private static final class MyDocumentListener implements DocumentListener {
        private final Runnable runnable;

        private MyDocumentListener(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            runnable.run();
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            runnable.run();
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            runnable.run();
        }
    }
}
