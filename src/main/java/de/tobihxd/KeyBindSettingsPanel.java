package de.tobihxd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class KeyBindSettingsPanel extends JPanel {
    private final LinkedHashMap<String, String> keyBinds = new LinkedHashMap<>();
    private final Map<String, JTextField> fieldMap = new LinkedHashMap<>();
    private final Map<String, JCheckBox> shiftMap = new LinkedHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();
    private Path localFile;

    public KeyBindSettingsPanel() {
        setLayout(new BorderLayout(10, 10));
        setupLocalFile();
        loadSettings();
        initUI();
    }

    /** Legt fest, wo die lokale Datei liegt */
    private void setupLocalFile() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            localFile = Path.of(System.getenv("APPDATA"), "Digital", "keybinds", "kb.json");
        } else {
            localFile = Path.of(System.getProperty("user.home"), ".digital", "keybinds", "kb.json");
        }

        try {
            Files.createDirectories(localFile.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Lädt Keybinds aus lokaler Datei oder Default-Resource */
    private void loadSettings() {
        try {
            if (Files.exists(localFile)) {
                try (InputStream in = Files.newInputStream(localFile)) {
                    Map<String, String> loaded = mapper.readValue(in, new TypeReference<Map<String, String>>() {});
                    keyBinds.putAll(loaded);
                    return;
                }
            }

            // Wenn keine lokale Datei existiert → Defaults aus Ressourcen
            URL resource = getClass().getClassLoader().getResource("keybinds/kb.json");
            if (resource == null) {
                JOptionPane.showMessageDialog(this, "Default keybind resource missing!", "Fehler",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (InputStream in = getClass().getClassLoader().getResourceAsStream("keybinds/kb.json")) {
                if (in != null) {
                    Map<String, String> defaults = mapper.readValue(in, new TypeReference<Map<String, String>>() {});
                    keyBinds.putAll(defaults);
                    saveSettings(); // lokale Kopie anlegen
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Fehler beim Laden der Keybinds:\n" + e.getMessage(),
                    "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Speichert aktuelle Keybinds in lokale Datei */
    private void saveSettings() {
        try (OutputStream out = Files.newOutputStream(localFile)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, keyBinds);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Fehler beim Speichern der Keybinds:\n" + e.getMessage(),
                    "Fehler", JOptionPane.ERROR_MESSAGE);
        }
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
        for (Map.Entry<String, String> entry : keyBinds.entrySet()) {
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
        scrollPane.setPreferredSize(new Dimension(400, 300)); // feste Größe
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);
    }

    /** Speichert Keybinds mit Validierung */
    private void saveKeybinds() {
        boolean allValid = true;

        for (Map.Entry<String, JTextField> entry : fieldMap.entrySet()) {
            String key = entry.getValue().getText().trim();
            JCheckBox shiftBox = shiftMap.get(entry.getKey());

            if (!isValidKey(key)) {
                allValid = false;
                entry.getValue().setBackground(Color.PINK);
            } else {
                entry.getValue().setBackground(Color.WHITE);
                String finalKey = (shiftBox.isSelected() ? "Shift+" : "") + key;
                keyBinds.put(entry.getKey(), finalKey);
            }
        }

        if (allValid) {
            saveSettings();
            JOptionPane.showMessageDialog(this, "Keybinds gespeichert!");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Ungültige Key(s) gefunden. Bitte korrigiere die markierten Felder.",
                    "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Prüft, ob der Key nur eine normale Taste ist (ohne Modifier) */
    private boolean isValidKey(String key) {
        return key.matches("[A-Z0-9]|F[1-9]|F1[0-2]|ENTER");
    }
    public LinkedHashMap<String, String> getKeyBinds() {
        return keyBinds;
    }
}
