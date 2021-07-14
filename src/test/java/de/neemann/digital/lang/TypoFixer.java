/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.lang;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static de.neemann.digital.lang.LanguageUpdater.FORMAT;

public class TypoFixer {
    private final File langPath;
    private final String fileToIgnore;
    private HashMap<File, Document> documents;

    public TypoFixer(File langPath, String fileToIgnore) {
        this.langPath = langPath;
        this.fileToIgnore = fileToIgnore;
    }

    /**
     * Replaces all en text fragments of key key with the newEnText.
     *
     * @param key       the key
     * @param newEnText the corrected en text
     */
    public void fix(String key, String newEnText) throws IOException, JDOMException {
        if (documents == null)
            initDocuments();

        for (Map.Entry<File, Document> e : documents.entrySet()) {
            System.out.println("fixed typo " + key + " in " + e.getKey());
            replace(e.getValue(), key, newEnText);
        }
    }

    private void initDocuments() throws IOException, JDOMException {
        String[] list = langPath.list((file, name) -> name.endsWith("_ref.xml") && !name.equals(fileToIgnore));

        documents = new HashMap<>();
        load("lang_en.xml");
        for (String n : list)
            load(n);
    }

    private void load(String name) throws IOException, JDOMException {
        File f = new File(langPath, name);
        Document doc = new SAXBuilder().build(f);
        documents.put(f, doc);
    }

    static boolean replace(Document xml, String key, String text) throws IOException {
        for (Element e : xml.getRootElement().getChildren()) {
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

    public void update() throws IOException {
        if (documents != null)
            for (Map.Entry<File, Document> e : documents.entrySet())
                new XMLOutputter(FORMAT).output(e.getValue(), new FileOutputStream(e.getKey()));
    }
}
