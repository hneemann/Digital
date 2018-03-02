/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.lang;

import de.neemann.gui.language.Bundle;
import de.neemann.gui.language.Language;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

/**
 * checks for duplicate language keys
 */
public class TestDuplicates extends TestCase {

    public void testDuplicates() throws IOException, ParserConfigurationException, SAXException {
        List<Language> languages = new Bundle("lang/lang").getSupportedLanguages();
        for (Language l : languages) {
            check(l.getName());
        }
    }

    private void check(String name) throws IOException, ParserConfigurationException, SAXException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("lang/lang_" + name + ".xml");
        if (in == null)
            throw new IOException("language file for " + name + " not found");
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            doc.getDocumentElement().normalize();
            NodeList list = doc.getDocumentElement().getElementsByTagName("string");

            HashSet<String> keys = new HashSet<>();
            for (int i = 0; i < list.getLength(); i++) {
                String key = list.item(i).getAttributes().getNamedItem("name").getNodeValue();

                if (keys.contains(key))
                    throw new RuntimeException("duplicate key " + key + " in lang file " + name);

                keys.add(key);
            }
        } finally {
            in.close();
        }
    }

}
