/*
 * Copyright (c) 2026 Digital contributors.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.draw.graphics.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Application-wide Light/Dark Swing theme helpers for MetalLookAndFeel.
 * Circuit colors continue to use {@link ColorScheme}.
 */
public final class Theme {
    private static final Color BG = new Color(43, 43, 43);
    private static final Color BG_ALT = new Color(50, 50, 50);
    private static final Color BG_INPUT = new Color(60, 63, 65);
    private static final Color BG_SELECTION = new Color(75, 110, 175);
    private static final Color FG = new Color(220, 220, 220);
    private static final Color FG_DISABLED = new Color(140, 140, 140);
    private static final Color BORDER = new Color(90, 90, 90);
    private static final Color TOOLTIP_BG = new Color(70, 70, 50);
    private static final Color TOOLTIP_FG = new Color(240, 240, 220);

    private static final Map<Object, Object> LIGHT_BACKUP = new HashMap<>();
    private static boolean dark;

    private Theme() {
    }

    /**
     * @return true if the Swing UI is currently in dark mode
     */
    public static boolean isDark() {
        return dark;
    }

    /**
     * @return true if the persisted color scheme is {@link ColorScheme.ColorSchemes#DARK}
     */
    public static boolean isDarkSchemeSelected() {
        return Settings.getInstance().get(ColorScheme.COLOR_SCHEME) == ColorScheme.ColorSchemes.DARK;
    }

    /**
     * Applies Swing dark/light defaults from the current Settings color scheme.
     * Call after LookAndFeel installation and before showing windows when possible.
     */
    public static void applyFromSettings() {
        if (isDarkSchemeSelected())
            applyDark();
        else
            applyLight();
    }

    /**
     * Enables or disables Dark Mode: updates ColorScheme persistence and Swing defaults.
     * Enabling sets the circuit color scheme to DARK.
     * Disabling sets it back to DEFAULT only if it was DARK (COLOR_BLIND/CUSTOM are left alone).
     *
     * @param enable true for dark mode
     */
    public static void setDarkMode(boolean enable) {
        if (enable) {
            Settings.getInstance().getAttributes().set(ColorScheme.COLOR_SCHEME, ColorScheme.ColorSchemes.DARK);
            applyDark();
        } else {
            if (isDarkSchemeSelected())
                Settings.getInstance().getAttributes().set(ColorScheme.COLOR_SCHEME, ColorScheme.ColorSchemes.DEFAULT);
            applyLight();
        }
        updateAllWindows();
    }

    /**
     * Syncs Swing theme to the current ColorScheme after Settings edits.
     * DARK → dark Swing chrome; any other scheme → light Swing chrome.
     */
    public static void syncSwingThemeToColorScheme() {
        if (isDarkSchemeSelected())
            applyDark();
        else
            applyLight();
        updateAllWindows();
    }

    private static void applyDark() {
        applyDarkSystemColors();
        applyDarkBasicControls();
        applyDarkTextControls();
        applyDarkMenus();
        applyDarkDataViews();
        applyDarkChrome();
        dark = true;
    }

    private static void applyDarkSystemColors() {
        put("control", BG);
        put("controlHighlight", new Color(70, 70, 70));
        put("controlShadow", new Color(30, 30, 30));
        put("controlDkShadow", new Color(20, 20, 20));
        put("controlLtHighlight", new Color(90, 90, 90));
        put("controlText", FG);
        put("info", TOOLTIP_BG);
        put("infoText", TOOLTIP_FG);
        put("menu", BG);
        put("menuText", FG);
        put("text", BG_INPUT);
        put("textText", FG);
        put("textHighlight", BG_SELECTION);
        put("textHighlightText", Color.WHITE);
        put("textInactiveText", FG_DISABLED);
        put("window", BG);
        put("windowText", FG);
        put("windowBorder", BORDER);
        put("desktop", new Color(35, 35, 35));
        put("scrollbar", BG_ALT);
        put("inactiveCaption", BG_ALT);
        put("inactiveCaptionText", FG_DISABLED);
        put("activeCaption", new Color(55, 55, 70));
        put("activeCaptionText", FG);
    }

    private static void applyDarkBasicControls() {
        putComponent("Panel", BG, FG);
        putComponent("Label", BG, FG);
        put("Label.disabledForeground", FG_DISABLED);

        putComponent("Button", BG_ALT, FG);
        put("Button.select", BG_SELECTION);
        put("Button.disabledText", FG_DISABLED);
        put("Button.focus", BG_SELECTION);

        putComponent("ToggleButton", BG_ALT, FG);
        put("ToggleButton.select", BG_SELECTION);
        put("ToggleButton.disabledText", FG_DISABLED);

        putComponent("CheckBox", BG, FG);
        put("CheckBox.disabledText", FG_DISABLED);
        putComponent("RadioButton", BG, FG);
        put("RadioButton.disabledText", FG_DISABLED);

        putComponent("ComboBox", BG_INPUT, FG);
        put("ComboBox.selectionBackground", BG_SELECTION);
        put("ComboBox.selectionForeground", Color.WHITE);
        put("ComboBox.disabledForeground", FG_DISABLED);
        put("ComboBox.buttonBackground", BG_ALT);
    }

    private static void applyDarkTextControls() {
        putComponent("TextField", BG_INPUT, FG);
        put("TextField.caretForeground", FG);
        put("TextField.selectionBackground", BG_SELECTION);
        put("TextField.selectionForeground", Color.WHITE);
        put("TextField.inactiveForeground", FG_DISABLED);
        put("TextField.inactiveBackground", BG_ALT);

        putComponent("PasswordField", BG_INPUT, FG);
        put("PasswordField.caretForeground", FG);
        put("PasswordField.selectionBackground", BG_SELECTION);
        put("PasswordField.selectionForeground", Color.WHITE);
        put("PasswordField.inactiveForeground", FG_DISABLED);

        putComponent("TextArea", BG_INPUT, FG);
        put("TextArea.caretForeground", FG);
        put("TextArea.selectionBackground", BG_SELECTION);
        put("TextArea.selectionForeground", Color.WHITE);
        put("TextArea.inactiveForeground", FG_DISABLED);

        putComponent("TextPane", BG_INPUT, FG);
        put("TextPane.caretForeground", FG);
        put("TextPane.selectionBackground", BG_SELECTION);
        put("TextPane.selectionForeground", Color.WHITE);

        putComponent("EditorPane", BG_INPUT, FG);
        put("EditorPane.caretForeground", FG);
        put("EditorPane.selectionBackground", BG_SELECTION);
        put("EditorPane.selectionForeground", Color.WHITE);

        putComponent("FormattedTextField", BG_INPUT, FG);
        put("FormattedTextField.caretForeground", FG);
        put("FormattedTextField.selectionBackground", BG_SELECTION);
        put("FormattedTextField.selectionForeground", Color.WHITE);
    }

    private static void applyDarkMenus() {
        putComponent("MenuBar", BG, FG);
        putComponent("Menu", BG, FG);
        put("Menu.selectionBackground", BG_SELECTION);
        put("Menu.selectionForeground", Color.WHITE);
        put("Menu.disabledForeground", FG_DISABLED);
        put("Menu.acceleratorForeground", FG);
        put("Menu.acceleratorSelectionForeground", Color.WHITE);

        putComponent("MenuItem", BG, FG);
        put("MenuItem.selectionBackground", BG_SELECTION);
        put("MenuItem.selectionForeground", Color.WHITE);
        put("MenuItem.disabledForeground", FG_DISABLED);
        put("MenuItem.acceleratorForeground", FG);
        put("MenuItem.acceleratorSelectionForeground", Color.WHITE);

        putComponent("CheckBoxMenuItem", BG, FG);
        put("CheckBoxMenuItem.selectionBackground", BG_SELECTION);
        put("CheckBoxMenuItem.selectionForeground", Color.WHITE);
        put("CheckBoxMenuItem.disabledForeground", FG_DISABLED);

        putComponent("RadioButtonMenuItem", BG, FG);
        put("RadioButtonMenuItem.selectionBackground", BG_SELECTION);
        put("RadioButtonMenuItem.selectionForeground", Color.WHITE);
        put("RadioButtonMenuItem.disabledForeground", FG_DISABLED);

        putComponent("PopupMenu", BG, FG);
        put("PopupMenu.border", BorderFactory.createLineBorder(BORDER));
    }

    private static void applyDarkDataViews() {
        putComponent("List", BG_INPUT, FG);
        put("List.selectionBackground", BG_SELECTION);
        put("List.selectionForeground", Color.WHITE);
        put("List.background", BG_INPUT);
        put("List.foreground", FG);

        putComponent("Tree", BG_INPUT, FG);
        put("Tree.selectionBackground", BG_SELECTION);
        put("Tree.selectionForeground", Color.WHITE);
        put("Tree.textBackground", BG_INPUT);
        put("Tree.textForeground", FG);
        put("Tree.hash", BORDER);
        put("Tree.line", BORDER);

        putComponent("Table", BG_INPUT, FG);
        put("Table.selectionBackground", BG_SELECTION);
        put("Table.selectionForeground", Color.WHITE);
        put("Table.gridColor", BORDER);
        putComponent("TableHeader", BG_ALT, FG);

        putComponent("TabbedPane", BG, FG);
        put("TabbedPane.selected", BG_ALT);
        put("TabbedPane.contentAreaColor", BG);
        put("TabbedPane.darkShadow", new Color(20, 20, 20));
        put("TabbedPane.light", BORDER);
        put("TabbedPane.highlight", BORDER);
        put("TabbedPane.shadow", new Color(30, 30, 30));
        put("TabbedPane.focus", BG_SELECTION);
    }

    private static void applyDarkChrome() {
        putComponent("ScrollPane", BG, FG);
        put("ScrollPane.background", BG);
        put("Viewport.background", BG);
        put("Viewport.foreground", FG);

        put("ScrollBar.background", BG);
        put("ScrollBar.foreground", FG);
        put("ScrollBar.track", BG);
        put("ScrollBar.thumb", new Color(100, 100, 100));
        put("ScrollBar.thumbShadow", new Color(60, 60, 60));
        put("ScrollBar.thumbHighlight", new Color(130, 130, 130));
        put("ScrollBar.thumbDarkShadow", new Color(40, 40, 40));

        putComponent("Slider", BG, FG);
        putComponent("Spinner", BG_INPUT, FG);
        put("Spinner.foreground", FG);
        put("Spinner.background", BG_INPUT);

        put("ToolTip.background", TOOLTIP_BG);
        put("ToolTip.foreground", TOOLTIP_FG);
        put("ToolTip.border", BorderFactory.createLineBorder(BORDER));

        putComponent("OptionPane", BG, FG);
        put("OptionPane.messageForeground", FG);

        putComponent("FileChooser", BG, FG);
        put("FileChooser.listViewBackground", BG_INPUT);

        putComponent("ProgressBar", BG_ALT, FG);
        put("ProgressBar.selectionBackground", BG_SELECTION);
        put("ProgressBar.selectionForeground", Color.WHITE);

        put("Separator.foreground", BORDER);
        put("Separator.background", BG);
        put("ToolBar.background", BG);
        put("ToolBar.foreground", FG);
        put("ToolBar.border", BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        put("ToolBar.floatingForeground", FG);
        put("ToolBar.dockingForeground", FG);

        put("TitledBorder.titleColor", FG);
        put("SplitPane.background", BG);
        put("SplitPaneDivider.draggingColor", BG_SELECTION);
    }

    private static void applyLight() {
        if (!LIGHT_BACKUP.isEmpty()) {
            for (Map.Entry<Object, Object> e : LIGHT_BACKUP.entrySet())
                UIManager.put(e.getKey(), e.getValue());
        }
        dark = false;
    }

    private static void putComponent(String name, Color background, Color foreground) {
        put(name + ".background", background);
        put(name + ".foreground", foreground);
    }

    private static void put(Object key, Object value) {
        if (!LIGHT_BACKUP.containsKey(key))
            LIGHT_BACKUP.put(key, UIManager.get(key));
        UIManager.put(key, value);
    }

    private static void updateAllWindows() {
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.invalidate();
            window.validate();
            window.repaint();
        }
    }
}
