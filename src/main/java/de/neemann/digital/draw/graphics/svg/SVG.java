package de.neemann.digital.draw.graphics.svg;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.neemann.digital.draw.graphics.Vector;

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
     * @param el
     *            List of XML Elements
     */
    public SVG(ArrayList<Element> el) {
        elements = "<svg>";
        for (Element e : el) {
            elements += elementToString(e);
        }
        elements += "</svg>";
    }

    /**
     * Moves a Pin
     * @param old
     *            old position
     * @param fresh
     *            new Position
     * @param id
     *            id of the pin
     * @param input
     *            if its a inputpin
     */
    public void transformPin(Vector old, Vector fresh, int id, boolean input) {
        elements = elements.replaceAll(
                "(<circle |<ellipse )[^\\/]*cx=\"" + old.x + "\"[^\\/]*cy=\"" + old.y + "\"[^\\/]*\\/>",
                "<circle cx=\"" + fresh.x + "\" cy=\"" + fresh.y + "\" r=\"2\" id=\"" + (input ? "input" : "output")
                        + id + "\"/>");
    }

    /**
     * Adds a new Pin
     * @param input
     *            Inputpin
     * @param number
     *            Number of the Pin
     * @param pos
     *            Position of the Pin
     */
    public void addPin(boolean input, int number, Vector pos) {
        String pin = "<circle cx=\"" + pos.x + "\" cy=\"" + pos.y + "\" id=\"" + (input ? "input" : "output") + number
                + "\" />";
        elements.replaceAll("</svg>", pin + "</svg>");
    }

    /**
     * Turns a XML Element to an XML String
     * @param e
     *            XML Element
     * @return XML String
     */
    private String elementToString(Element e) {
        String tmp = "<" + e.getNodeName();
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
        return ImportSVG.getElements(svg);
    }
}
