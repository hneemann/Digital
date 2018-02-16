package de.neemann.digital.draw.graphics.svg;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Representation of a SVG. Is used to be saved in the Circuit-properties
 * @author felix
 */
public class SVG {
    private String elements = "";

    /**
     * Stock Constructor
     */
    public SVG() {

    }

    /**
     * Creates a SVG Object from a list of XML Elements
     * @param list
     *            List of XML Elements
     */
    public SVG(ArrayList<Element> list) {
        elements = "<root>";
        for (Element e : list) {
            elements += elementToString(e);
        }
        elements += "</root>";
    }

    /**
     * Turns a XML Element to an XML String
     * @param e
     *            XML Element
     * @return XML String
     */
    private String elementToString(Element e) {
        String tmp = "<" + e.getNodeName() + " ";
        for (int i = 0; i < e.getAttributes().getLength(); i++) {
            tmp += " " + e.getAttributes().item(i).getNodeName() + "=";
            tmp += "\"" + e.getAttributes().item(i).getNodeValue() + "\"";
        }
        if (e.getElementsByTagName("*").getLength() > 0) {
            tmp += " >";
            NodeList gList = e.getElementsByTagName("*");
            for (int i = 0; i < gList.getLength(); i++) {
                tmp += elementToString((Element) gList.item(i));
            }
            tmp += "</" + e.getNodeName() + ">";
        } else if (e.getTextContent().isEmpty())
            tmp += " />";
        else {
            tmp += ">";
            tmp += e.getTextContent();
            tmp += "</" + e.getNodeName() + ">";
        }
        return tmp;
    }

    /**
     * Is True, if the SVG Object is filled
     * @return elementsstring is not empty
     */
    public boolean isSet() {
        return !elements.isEmpty();
    }

    /**
     * Get the Elements as XML Elements
     * @return List of XML Elements
     * @throws NoParsableSVGException
     *             if the saved XML is not correct
     */
    public ArrayList<Element> getElements() throws NoParsableSVGException {
        Document svg;
        try {
            svg = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(elements.getBytes(Charset.defaultCharset())));
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
        svg.getDocumentElement().normalize();
        NodeList gList;
        try {
            gList = svg.getElementsByTagName("*");
        } catch (Exception e) {
            throw new NoParsableSVGException();
        }
        ArrayList<Element> ret = new ArrayList<Element>();
        for (int i = 0; i < gList.getLength(); i++) {
            if (!gList.item(i).getNodeName().equals("root"))
                ret.add((Element) gList.item(i));
        }
        return ret;
    }
}
