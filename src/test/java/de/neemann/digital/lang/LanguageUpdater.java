/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.lang;

import de.neemann.digital.integration.Resources;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class LanguageUpdater {
    private final Document lang;
    private final Document ref;
    private final File langFileName;
    private final File refFileName;
    private int modified;

    private LanguageUpdater(File sourceFilename) throws JDOMException, IOException {
        Element dif = new SAXBuilder().build(sourceFilename).getRootElement();
        String langName = dif.getAttributeValue("name");

        File langPath = new File(Resources.getRoot(), "../../main/resources/lang");
        if (!langPath.exists())
            throw new IOException("lang folder not found!");

        langFileName = new File(langPath, "lang_" + langName + ".xml");
        refFileName = new File(langPath, "lang_" + langName + "_ref.xml");
        lang = new SAXBuilder().build(langFileName);
        ref = new SAXBuilder().build(refFileName);

        for (Element e : (List<Element>) dif.getChildren()) {
            String key = e.getAttributeValue("name");
            String type = e.getAttributeValue("type");
            final String text = e.getChild(langName).getText().trim();
            if (!text.isEmpty() && !text.equals("-")) {
                if (type.equals("new")) {
                    add(ref, key, e.getChild("en").getText());
                    add(lang, key, text);
                    modified++;
                } else {
                    if (replace(lang, key, text)) {
                        replace(ref, key, e.getChild("en").getText());
                        modified++;
                    } else {
                        System.out.println("ignored unchanged key: " + key);
                    }
                }
            } else {
                System.out.println("ignored empty key: " + key);
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

    private boolean replace(Document xml, String key, String text) throws IOException {
        for (Element e : (List<Element>) xml.getRootElement().getChildren()) {
            String k = e.getAttributeValue("name");
            if (k.equals(key)) {
                if (e.getText().trim().equals(text.trim())) {
                    return false;
                } else {
                    e.setText(text);
                    return true;
                }
            }
        }
        throw new IOException("key " + key + " not found in " + xml);
    }

    private void update() throws IOException {
        if (modified > 0) {
            new XMLOutputter(Format.getPrettyFormat()).output(ref, new FileOutputStream(refFileName));
            new XMLOutputter(Format.getPrettyFormat()).output(lang, new FileOutputStream(langFileName));
            System.out.println(modified + " keys updated!");
        } else {
            System.out.println("no modification found!");
        }
    }

    public static void main(String[] args) throws JDOMException, IOException {
        JFileChooser dc = new JFileChooser();
        dc.setDialogTitle("Select the Digital \"src\" Folder");
        dc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File src = null;
        if (dc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            src = dc.getSelectedFile();
            System.setProperty("testdata", new File(src, "test/resources").getPath());
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select the updated diff File");
        fc.addChoosableFileFilter(new FileNameExtensionFilter("xml", "xml"));
        if (src != null) {
            final File s = new File(src.getParentFile(), "target/lang_diff_pt.xml");
            fc.setSelectedFile(s);
        }
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            new LanguageUpdater(fc.getSelectedFile()).update();
    }

}
