/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.lang;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.draw.library.ElementLibrary;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class KeyOrderer {

    private static void orderKeys(String name) throws IOException, ParserConfigurationException, SAXException {
        InputStream in = ClassLoader.getSystemResourceAsStream("lang/lang_" + name + ".xml");
        if (in == null)
            throw new IOException("language file for " + name + " not found");
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            doc.getDocumentElement().normalize();
            NodeList list = doc.getDocumentElement().getElementsByTagName("string");

            ArrayList<Entry> keys = new ArrayList<>();
            for (int i = 0; i < list.getLength(); i++) {
                final Node item = list.item(i);
                String key = item.getAttributes().getNamedItem("name").getNodeValue();
                String value = item.getTextContent();

//                if (key.startsWith("key_"))
                    keys.add(new Entry(key, value));
            }

//            Collections.sort(keys);
            writeKeysPlain(keys, System.out);

        } finally {
            in.close();
        }
    }

    private static void writeKeysPlain(ArrayList<Entry> keys, PrintStream out) {
        for (Entry e : keys) {
//            out.print(e.key);
//            out.println(":");
            out.println(e.value.trim());
            out.println();
        }
    }

    private static void writeKeysXML(ArrayList<Entry> keys, PrintStream out) {
        for (Entry e : keys) {
            out.print("    <string name=\"");
            out.print(escapeXML(e.key, true));
            out.print("\">");
            out.print(escapeXML(e.value.trim(), false));
//            if (e.value.contains("\n"))
//                out.print("\n    ");
            out.print("</string>");

            if (e.elements != null) {
                out.print("<!-- ");
                boolean first = true;
                for (ElementTypeDescription d : e.elements) {
                    if (first)
                        first = false;
                    else
                        out.print(", ");
                    out.print(d.getName());
                }
                out.print(" -->");
            }

            out.println();
        }
    }

    private static final class Entry implements Comparable<Entry> {
        private static final ElementLibrary lib = new ElementLibrary();

        private final String key;
        private final String value;
        private ArrayList<ElementTypeDescription> elements;

        private Entry(String key, String value) {
            this.key = key;
            this.value = value;

            if (key.startsWith("key_")) {
                for (ElementLibrary.ElementContainer el : lib) {
                    for (Key k : el.getDescription().getAttributeList()) {
                        if (k.getLangKey().equals(key)) {
                            if (elements == null)
                                elements = new ArrayList<>();
                            elements.add(el.getDescription());
                        }
                    }
                }
            }

        }

        @Override
        public String toString() {
            return "Entry{" +
                    "key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    ", used='" + elements + '\'' +
                    '}';
        }

        @Override
        public int compareTo(Entry entry) {
            return key.compareToIgnoreCase(entry.key);
        }

        private String longName() {
            return elements.get(0).getName() + "|" + key;
        }
    }

    private static String escapeXML(String text, boolean isAttribute) {
        StringBuilder sb = new StringBuilder(text.length() * 2);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    if (isAttribute) {
                        sb.append("&quot;");
                        break;
                    }
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }


    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        orderKeys("de");
    }

}
