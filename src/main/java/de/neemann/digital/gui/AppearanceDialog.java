/*
 * Copyright (c) 2026 Michachatz.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.EnumMap;
import java.util.Map;

/**
 * Dialog for selecting the application's appearance.
 */
final class AppearanceDialog extends JDialog {

    AppearanceDialog(Frame owner) {
        super(owner, Lang.get("appearance_title"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        ButtonGroup group = new ButtonGroup();
        Map<Appearance.Mode, JRadioButton> choices = new EnumMap<>(Appearance.Mode.class);
        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        for (Appearance.Mode mode : Appearance.Mode.values()) {
            JRadioButton choice = new JRadioButton(Lang.get("key_appearance_" + mode.name()));
            group.add(choice);
            options.add(choice);
            choices.put(mode, choice);
        }
        choices.get(Settings.getInstance().get(Appearance.MODE)).setSelected(true);
        getContentPane().add(options, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(new AbstractAction(Lang.get("cancel")) {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        }));
        buttons.add(new JButton(new AbstractAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent event) {
                Appearance.Mode selected = getSelectedMode(choices);
                try {
                    Appearance.apply(selected);
                    Settings.getInstance().getAttributes().set(Appearance.MODE, selected);
                    dispose();
                } catch (IllegalStateException e) {
                    JOptionPane.showMessageDialog(AppearanceDialog.this, e.getMessage(),
                            Lang.get("appearance_title"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }));
        getContentPane().add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private static Appearance.Mode getSelectedMode(Map<Appearance.Mode, JRadioButton> choices) {
        for (Map.Entry<Appearance.Mode, JRadioButton> entry : choices.entrySet())
            if (entry.getValue().isSelected())
                return entry.getKey();
        throw new IllegalStateException("No appearance selected");
    }
}
