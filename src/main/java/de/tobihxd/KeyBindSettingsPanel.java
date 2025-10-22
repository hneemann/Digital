package de.tobihxd;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class KeyBindSettingsPanel extends JPanel {
    private final Map<String, JTextField> fieldMap = new LinkedHashMap<>();
    private final Map<String, JCheckBox> shiftMap = new LinkedHashMap<>();

    private final KeybindManager manager = KeybindManager.getInstance();

    public KeyBindSettingsPanel() {
        setLayout(new BorderLayout(10, 10));
        initUI();
    }

    /** Erstellt UI */
    private void initUI() {
        JPanel listPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        for (Map.Entry<String, String> entry : manager.getKeyBinds().entrySet()) {
            JLabel label = new JLabel(entry.getKey());
            String value = entry.getValue();
            boolean shiftUsed = value.startsWith("Shift+");
            String keyOnly = shiftUsed ? value.substring(6) : value;

            JTextField field = new JTextField(keyOnly, 10);
            JCheckBox shiftBox = new JCheckBox("Shift", shiftUsed);

            fieldMap.put(entry.getKey(), field);
            shiftMap.put(entry.getKey(), shiftBox);

            gbc.gridx = 0;
            gbc.gridy = row;
            listPanel.add(label, gbc);

            gbc.gridx = 1;
            listPanel.add(field, gbc);

            gbc.gridx = 2;
            listPanel.add(shiftBox, gbc);

            row++;
        }

        JButton saveButton = new JButton("Speichern");
        saveButton.addActionListener(e -> saveKeybinds());

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);
    }

    /** Speichert Keybinds mit Validierung über den Manager */
    private void saveKeybinds() {
        boolean allValid = true;

        for (Map.Entry<String, JTextField> entry : fieldMap.entrySet()) {
            String key = entry.getValue().getText().trim().toUpperCase();
            JCheckBox shiftBox = shiftMap.get(entry.getKey());

            if (!manager.isValidKey(key)) {
                allValid = false;
                entry.getValue().setBackground(Color.PINK);
            } else {
                entry.getValue().setBackground(Color.WHITE);
                String finalKey = (shiftBox.isSelected() ? "Shift+" : "") + key;
                manager.setKeyBind(entry.getKey(), finalKey);
            }
        }

        if (allValid) {
            manager.save();

            JOptionPane.showMessageDialog(this, "Keybinds gespeichert!");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Ungültige Key(s) gefunden. Bitte korrigiere die markierten Felder.",
                    "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }
}
