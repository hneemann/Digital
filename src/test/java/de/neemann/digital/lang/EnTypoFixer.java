/*
 * Copyright (c) 2020 Helmut Neemann.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Is used to distribute typo fixes in the en lang file to all of the ref files.
 */
public class EnTypoFixer {

    /**
     * Start only if there are ONLY typo fixes in the lang_en.xml, which are to propagate to the
     * lang_xx_ref.xml files!!!
     * To insure this, make sure that all of the current dif files don't contain a "modified" entry!
     * If on of the dif files contains a modified entry this modification is lost!
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JDOMException {
        File langPath = new File(Resources.getRoot(), "../../main/resources/lang");
        if (!langPath.exists())
            throw new IOException("lang folder not found!");

        File enFileName = new File(langPath, "lang_en.xml");
        Document en = new SAXBuilder().build(enFileName);
        HashMap<String, String> enMap = createMap(en);
        System.out.println(enMap);
        String[] list = langPath.list((file, name) -> name.endsWith("_ref.xml"));
        for (String name : list) {
            File refFileName = new File(langPath, name);
            Document ref = new SAXBuilder().build(refFileName);
            fix(enMap, ref);
            new XMLOutputter(Format.getPrettyFormat()).output(ref, new FileOutputStream(refFileName));
        }
    }

    private static HashMap<String, String> createMap(Document lang) {
        HashMap<String, String> map = new HashMap<>();
        for (Element e : lang.getRootElement().getChildren()) {
            String key = e.getAttributeValue("name");
            String text = e.getText();
            map.put(key, text);
        }
        return map;
    }

    private static void fix(HashMap<String, String> en, Document ref) {
        for (Element e : ref.getRootElement().getChildren()) {
            String key = e.getAttributeValue("name");
            String newEn = en.get(key);
            if (newEn != null)
                e.setText(newEn);
        }
    }
}
