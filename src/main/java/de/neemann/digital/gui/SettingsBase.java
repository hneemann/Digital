/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import de.neemann.digital.core.element.AttributeListener;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.draw.elements.Circuit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Base class for Settings
 */
public class SettingsBase implements AttributeListener {

    private final ElementAttributes attributes;
    private final File filename;
    private final List<Key> settingsKeys;

    /**
     * Creates a new instance
     *
     * @param settingsKeys the list of keys
     * @param name         the filename
     */
    protected SettingsBase(List<Key> settingsKeys, String name) {
        this.settingsKeys = settingsKeys;

        filename = new File(new File(System.getProperty("user.home")), name);

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
        try (Writer out = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8)) {
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
