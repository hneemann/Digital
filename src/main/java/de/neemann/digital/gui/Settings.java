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

    private static final List<Key> INT_LIST = new ArrayList<>();

    /**
     * The list of supported attributes.
     */
    public static final List<Key> SETTINGS_KEYS = Collections.unmodifiableList(INT_LIST);


    static {
        INT_LIST.add(Keys.SETTINGS_IEEE_SHAPES);
        INT_LIST.add(Keys.SETTINGS_LANGUAGE);
        INT_LIST.add(Keys.SETTINGS_EXPRESSION_FORMAT);
        INT_LIST.add(Keys.SETTINGS_DEFAULT_TREESELECT);
        INT_LIST.add(Keys.SETTINGS_ATF1502_FITTER);
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
}

