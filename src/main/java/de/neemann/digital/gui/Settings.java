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

/**
 * The Settings of Digital
 * <p/>
 * Created by Helmut.Neemann on 11.05.2016.
 */
public final class Settings implements AttributeListener {

    /**
     * The list of supported attributes.
     */
    public static final ArrayList<Key> SETTINGS_KEYS = new ArrayList<>();

    static {
        SETTINGS_KEYS.add(Keys.SETTINGS_IEEE_SHAPES);
    }

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

    private ElementAttributes attributes;
    private final File filename;

    private Settings() {
        filename = new File(new File(System.getProperty("user.home")), ".digital.cfg");

        XStream xStream = Circuit.getxStream();
        try (InputStream in = new FileInputStream(filename)) {
            attributes = (ElementAttributes) xStream.fromXML(in);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (attributes == null) {
            System.out.println("Use default settings!");
            attributes = new ElementAttributes();
        }

        attributes.addListener(this);
    }

    /**
     * @return the settings
     */
    public ElementAttributes getAttributes() {
        return attributes;
    }

    @Override
    public void attributeChanged(Key key) {
        XStream xStream = Circuit.getxStream();
        try (Writer out = new OutputStreamWriter(new FileOutputStream(filename), "utf-8")) {
            out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            xStream.marshal(attributes, new PrettyPrintWriter(out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

