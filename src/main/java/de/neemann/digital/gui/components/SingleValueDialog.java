/*
 * Copyright (c) 2016 Helmut Neemann, Rüdiger Heintz
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.*;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.Screen;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

/**
 * Dialog to edit a single value.
 * Used to enter a multi bit input value.
 */
public final class SingleValueDialog extends JDialog implements ModelStateObserverTyped {

    private final ObservableValue value;
    private final SyncAccess syncAccess;

    private enum InMode {
        HEX(Lang.get("attr_dialogHex")),
        DECIMAL(Lang.get("attr_dialogDecimal")),
        BIN(Lang.get("attr_dialogBinary")),
        OCTAL(Lang.get("attr_dialogOctal")),
        ASCII(Lang.get("attr_dialogAscii")),
        HIGHZ(Lang.get("attr_dialogHighz"));

        private final String langText;

        InMode(String langKey) {
            this.langText = langKey;
        }

        @Override
        public String toString() {
            return langText;
        }

        private static InMode[] values(boolean supportsHighZ) {
            if (supportsHighZ) {
                return values();
            } else {
                return Arrays.copyOf(values(), values().length - 1);
            }
        }

        private static InMode getByFormat(IntFormat format) {
            switch (format) {
                case decSigned:
                case dec:
                    return SingleValueDialog.InMode.DECIMAL;
                case oct:
                    return SingleValueDialog.InMode.OCTAL;
                case bin:
                    return SingleValueDialog.InMode.BIN;
                case ascii:
                    return SingleValueDialog.InMode.ASCII;
                default:
                    return SingleValueDialog.InMode.HEX;
            }
        }
    }

    private final JTextField textField;
    private boolean textIsModifying;
    private final boolean supportsHighZ;
    private final JComboBox<InMode> formatComboBox;
    private final long mask;
    private JCheckBox[] checkBoxes;
    private long editValue;

    /**
     * Edits a single value
     *
     * @param parent        the parent frame
     * @param pos           the position to pop up the dialog
     * @param label         the name of the value
     * @param value         the value to edit
     * @param supportsHighZ true is high z is supported
     * @param model         the model
     */
    public SingleValueDialog(JFrame parent, Point pos, String label, ObservableValue value, boolean supportsHighZ, Model model) {
        super(parent, Lang.get("win_valueInputTitle_N", label), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.value = value;
        this.syncAccess = model;

        editValue = value.getValue();
        this.supportsHighZ = supportsHighZ;
        mask = Bits.mask(value.getBits());

        textField = new JTextField(10);
        textField.setHorizontalAlignment(JTextField.RIGHT);

        formatComboBox = new JComboBox<>(InMode.values(supportsHighZ));
        formatComboBox.addActionListener(actionEvent -> setLongToDialog(editValue));

        model.modify(() -> model.addObserver(this));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                model.modify(() -> model.removeObserver(SingleValueDialog.this));
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        ConstraintsBuilder constr = new ConstraintsBuilder().inset(3).fill();
        panel.add(formatComboBox, constr);
        JSpinner spinner = new JSpinner(new MySpinnerModel()) {
            @Override
            protected JComponent createEditor(SpinnerModel spinnerModel) {
                return textField;
            }
        };
        panel.add(spinner, constr.dynamicWidth().x(1));
        constr.nextRow();
        panel.add(new JLabel(Lang.get("attr_dialogBinary")), constr);
        panel.add(createCheckBoxPanel(value.getBits(), editValue), constr.dynamicWidth().x(1));
        getContentPane().add(panel);

        textField.getDocument().addDocumentListener(new MyDocumentListener(() -> setStringToDialog(textField.getText())));

        if (value.isHighZ())
            formatComboBox.setSelectedItem(InMode.HIGHZ);
        else
            setLongToDialog(editValue);

        JButton okButton = new JButton(new AbstractAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                apply();
                dispose();
            }
        });
        final AbstractAction applyAction = new AbstractAction(Lang.get("btn_apply")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                apply();
            }
        };
        JButton applyButton = new JButton(applyAction);
        textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK, true), applyAction);
        textField.getActionMap().put(applyAction, applyAction);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
        buttonPanel.add(okButton);
        buttonPanel.add(applyButton);
        getContentPane().add(buttonPanel, BorderLayout.EAST);

        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(actionEvent -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        Screen.setLocation(this, pos, true);
        textField.requestFocus();
        textField.select(0, Integer.MAX_VALUE);
    }

    private void apply() {
        if (getSelectedFormat().equals(InMode.HIGHZ)) {
            syncAccess.modify(value::setToHighZ);
        } else {
            syncAccess.modify(() -> value.setValue(editValue));
        }
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (event.equals(ModelEvent.CLOSED))
            dispose();
    }

    @Override
    public ModelEventType[] getEvents() {
        return new ModelEventType[]{ModelEventType.CLOSED};
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
        if (!textIsModifying) {
            switch (getSelectedFormat()) {
                case ASCII:
                    char val = (char) (editValue);
                    textField.setText("'" + val + "'");
                    textField.setCaretPosition(1);
                    break;
                case DECIMAL:
                    textField.setText(Long.toString(editValue));
                    break;
                case HEX:
                    textField.setText("0x" + Long.toHexString(editValue));
                    break;
                case BIN:
                    textField.setText("0b" + Long.toBinaryString(editValue));
                    break;
                case OCTAL:
                    textField.setText("0" + Long.toOctalString(editValue));
                    break;
                case HIGHZ:
                    textField.setText("Z");
                    break;
                default:
            }
            textField.requestFocus();
        }
    }

    private InMode getSelectedFormat() {
        return (InMode) formatComboBox.getSelectedItem();
    }

    /**
     * Sets the selected format
     *
     * @param format the format
     * @return this for chained calls
     */
    public SingleValueDialog setSelectedFormat(IntFormat format) {
        setSelectedFormat(InMode.getByFormat(format));
        return this;
    }

    private void setSelectedFormat(InMode format) {
        if (!getSelectedFormat().equals(format))
            formatComboBox.setSelectedItem(format);
    }

    private void setStringToDialog(String text) {
        text = text.trim();
        if (text.length() > 0) {
            if (text.toLowerCase().contains("z") && supportsHighZ) {
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
                else if (text.startsWith("0b"))
                    setSelectedFormat(InMode.BIN);
                else if (text.startsWith("0") && text.length() > 1)
                    setSelectedFormat(InMode.OCTAL);
                else
                    setSelectedFormat(InMode.DECIMAL);
                try {
                    editValue = Bits.decode(text);
                } catch (Bits.NumberFormatException e) {
                    // do nothing on error
                }
            }
            for (int i = 0; i < checkBoxes.length; i++)
                checkBoxes[i].setSelected((editValue & (1L << i)) != 0);
        }
    }

    private final class MyDocumentListener implements DocumentListener {
        private final Runnable runnable;

        private MyDocumentListener(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            run();
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            run();
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            run();
        }

        private void run() {
            textIsModifying = true;
            runnable.run();
            textIsModifying = false;
        }
    }

    private class MySpinnerModel implements SpinnerModel {
        @Override
        public Object getValue() {
            return editValue;
        }

        @Override
        public void setValue(Object o) {
            if (o instanceof Number) {
                editValue = ((Number) o).longValue();
                setLongToDialog(editValue);
                apply();
            }
        }

        @Override
        public Object getNextValue() {
            return (editValue + 1) & mask;
        }

        @Override
        public Object getPreviousValue() {
            return (editValue - 1) & mask;
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
        }
    }
}
