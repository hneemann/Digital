/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.lang;

import de.neemann.digital.integration.Resources;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class LanguageUpdater {
    private final Document lang;
    private final Document ref;
    private final File langFileName;
    private final File refFileName;

    public LanguageUpdater(File sourceFilename) throws JDOMException, IOException {
        Document dif = new SAXBuilder().build(sourceFilename);
        String langName = dif.getRootElement().getAttributeValue("name");

        File langPath = new File(Resources.getRoot(), "../../main/resources/lang");
        if (!langPath.exists())
            throw new IOException("lang folder not found!");

        langFileName = new File(langPath, "lang_" + langName + ".xml");
        refFileName = new File(langPath, "lang_" + langName + "_ref.xml");
        lang = new SAXBuilder().build(langFileName);
        ref = new SAXBuilder().build(refFileName);


        for (Element e : (List<Element>) dif.getRootElement().getChildren()) {
            String key = e.getAttributeValue("name");
            String type = e.getAttributeValue("type");
            final String text = e.getChild(langName).getText().trim();
            if (!text.isEmpty() && !text.equals("-")) {
                if (type.equals("new")) {
                    add(ref, key, e.getChild("en").getText());
                    add(lang, key, text);
                } else {
                    replace(ref, key, e.getChild("en").getText());
                    replace(lang, key, text);
                }
            }
        }

    }

    private void add(Document xml, String key, String text) throws IOException {
        for (Element e : (List<Element>) xml.getRootElement().getChildren()) {
            String k = e.getAttributeValue("name");
            if (k.equals(key)) {
                throw new IOException("key " + key + " is already present in " + xml);
            }
        }
        xml.getRootElement().addContent("    ");
        xml.getRootElement().addContent(new Element("string").setAttribute("name", key).setText(text));
        xml.getRootElement().addContent("\n");
    }

    private void replace(Document xml, String key, String text) throws IOException {
        for (Element e : (List<Element>) xml.getRootElement().getChildren()) {
            String k = e.getAttributeValue("name");
            if (k.equals(key)) {
                e.setText(text);
                return;
            }
        }
        throw new IOException("key " + key + " not found in " + xml);
    }

    private void update() throws IOException {
        new XMLOutputter(Format.getPrettyFormat()).output(ref, new FileOutputStream(refFileName));
        new XMLOutputter(Format.getPrettyFormat()).output(lang, new FileOutputStream(langFileName));
    }

    public static void main(String[] args) throws JDOMException, IOException {
        new LanguageUpdater(new File("/home/hneemann/Dokumente/Java/digital/target/language_zz.xml")).update();
    }

}
