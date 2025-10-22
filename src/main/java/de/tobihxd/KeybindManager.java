package de.tobihxd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.gui.InsertAction;
import de.neemann.digital.gui.LibrarySelector;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class KeybindManager {
    private static KeybindManager instance;
    private final LinkedHashMap<String, String> keyBinds = new LinkedHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private ElementLibrary library;
    private Path localFile;

    private KeybindManager() {
        setupLocalFile();
        load();
    }
    private KeybindManager(ElementLibrary library){
        this.library = library;
        setupLocalFile();
        load();
    }

    public static void createInstance(ElementLibrary library){
        instance = new KeybindManager(library);
    }
    public static KeybindManager getInstance() {
        if (instance == null) {
            instance = new KeybindManager();
        }
        return instance;
    }

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

    public void load() {
        try {
            if (Files.exists(localFile)) {
                try (InputStream in = Files.newInputStream(localFile)) {
                    Map<String, String> loaded = mapper.readValue(in, new TypeReference<>() {
                    });
                    keyBinds.clear();
                    keyBinds.putAll(loaded);
                    return;
                }
            }

            URL resource = getClass().getClassLoader().getResource("keybinds/kb.json");
            if (resource != null) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("keybinds/kb.json")) {
                    if (in != null) {
                        Map<String, String> defaults = mapper.readValue(in, new TypeReference<>() {
                        });
                        keyBinds.clear();
                        keyBinds.putAll(defaults);
                        save(); // lokale Kopie anlegen
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (OutputStream out = Files.newOutputStream(localFile)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, keyBinds);
            updateLibraryKeybinds();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLibraryKeybinds() {
        //library.getRoot().getChild(0).getChild(0).setKey("Q");
        //library.fireLibraryChanged(library.getRoot().getChild(0).getChild(0));
        LibrarySelector selector = library.getLibraryListener();
        if (selector == null)
            return;
        JMenu componentsMenu = selector.getComponentsMenu();
        JMenuItem menuComponent = getJMenuByName("Und", componentsMenu);
        //menuComponent.setAccelerator(KeyStroke.getKeyStroke("Z"));
        ActionListener[] actions = menuComponent.getListeners(ActionListener.class);
        for (ActionListener action: actions) {
            //action.disableAcceleratorIn(selector.getMain().getCircuitComponent());
            ((InsertAction)action).setAccelerator("Q").enableAcceleratorIn(selector.getMain().getCircuitComponent());
            selector.getMain().getCircuitComponent().getInputMap().remove(KeyStroke.getKeyStroke("A"));
        }
    }

    private JMenuItem getJMenuByName(String name, JMenu component){
        System.out.println(component.getText());
        for (int i = 0; i < component.getItemCount(); i++) {
            JMenuItem instance = component.getItem(i);
            if (instance.getText().equals(name))
                return instance;
            if (instance instanceof JMenu sub){
                return getJMenuByName(name, sub);
            }
        }
        return null;
    }

    public LinkedHashMap<String, String> getKeyBinds() {
        return keyBinds;
    }

    public void setKeyBind(String action, String key) {
        if (isValidKey(key)) {
            keyBinds.put(action, key);
        }
    }

    public boolean isValidKey(String key) {
        return key.matches("(Shift\\+)?([A-Z0-9]|F[1-9]|F1[0-2]|ENTER)");
    }
}
