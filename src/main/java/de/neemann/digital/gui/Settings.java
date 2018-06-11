/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import de.neemann.digital.core.element.AttributeListener;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Settings of Digital
 * <p>
 * Created by Helmut.Neemann on 11.05.2016.
 */
public final class Settings implements AttributeListener {

    private static final class SettingsHolder {
        static final Settings INSTANCE = new Settings();
    }

    /**
     * Returns the settings instance
     *
     * @return the Settings
     */
    public static Settings getInstance() {
        return SettingsHolder.INSTANCE;
    }

    private final ElementAttributes attributes;
    private final File filename;
    private final List<Key> settingsKeys;

    private Settings() {
        List<Key> intList = new ArrayList<>();
        intList.add(Keys.SETTINGS_IEEE_SHAPES);
        intList.add(Keys.SETTINGS_LANGUAGE);
        intList.add(Keys.SETTINGS_EXPRESSION_FORMAT);
        intList.add(Keys.SETTINGS_DEFAULT_TREESELECT);
        intList.add(Keys.SETTINGS_GRID);
        intList.add(Keys.SETTINGS_SHOW_WIRE_BITS);
        intList.add(Keys.SETTINGS_NOTOOLTIPS);
        intList.add(Keys.SETTINGS_LIBRARY_PATH);
        intList.add(Keys.SETTINGS_JAR_PATH);
        intList.add(Keys.SETTINGS_ATF1502_FITTER);
        intList.add(Keys.SETTINGS_ATMISP);
        intList.add(Keys.SETTINGS_GHDL_PATH);
        intList.add(Keys.SETTINGS_IVERILOG_PATH);
        intList.add(Keys.SETTINGS_FONT_SCALING);
        intList.add(Keys.SETTINGS_MAC_MOUSE);

        settingsKeys = Collections.unmodifiableList(intList);

        filename = new File(new File(System.getProperty("user.home")), ".digital.cfg");

        ElementAttributes attr = null;
        if (filename.exists()) {
            XStream xStream = Circuit.getxStream();
            try (InputStream in = new FileInputStream(filename)) {
                attr = (ElementAttributes) xStream.fromXML(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (attr == null) {
            System.out.println("Use default settings!");
            attributes = new ElementAttributes();
        } else
            attributes = attr;

        attributes.addListener(this);
    }

    /**
     * @return the settings
     */
    public ElementAttributes getAttributes() {
        return attributes;
    }

    /**
     * Gets a value from the settings.
     * If the value is not present the default value is returned
     *
     * @param key     the key
     * @param <VALUE> the type of the value
     * @return the value
     */
    public <VALUE> VALUE get(Key<VALUE> key) {
        return attributes.get(key);
    }

    @Override
    public void attributeChanged() {
        XStream xStream = Circuit.getxStream();
        try (Writer out = new OutputStreamWriter(new FileOutputStream(filename), "utf-8")) {
            out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            xStream.marshal(attributes, new PrettyPrintWriter(out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the settings keys
     */
    public List<Key> getKeys() {
        return settingsKeys;
    }

}

