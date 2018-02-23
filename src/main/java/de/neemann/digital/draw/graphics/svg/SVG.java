package de.neemann.digital.draw.graphics.svg;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;

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
     * Creates a new SVG with old Data
     * @param renew
     *            elements String
     */
    public SVG(String renew) {
        this.elements = renew;
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
     * @param label
     *            Name of the pin
     * @param input
     *            if its a inputpin
     * @return SVG
     */
    public SVG transformPin(Vector old, Vector fresh, String label, boolean input) {
        elements = elements.replaceAll(
                "(<circle |<ellipse )[^\\/]*cx=\"" + old.x + "\"[^\\/]*cy=\"" + old.y + "\"[^\\/]*\\/>",
                "<circle cx=\"" + fresh.x + "\" cy=\"" + fresh.y + "\" r=\"2\" id=\"" + (input ? "input:" : "output:")
                        + label + "\"/>");
        return new SVG(elements);
    }

    /**
     * Deletes a Pin
     * @param old
     *            old position
     * @return SVG
     */
    public SVG deletePin(Vector old) {
        elements = elements.replaceAll(
                "(<circle |<ellipse )[^\\/]*cx=\"" + old.x + "\"[^\\/]*cy=\"" + old.y + "\"[^\\/]*\\/>", "");
        return new SVG(elements);
    }

    /**
     * Adds a new Pin
     * @param input
     *            Inputpin
     * @param label
     *            Name of the Pin
     * @param pos
     *            Position of the Pin
     * @return SVG
     */
    public SVG addPin(boolean input, String label, Vector pos) {
        String pin = "<circle cx=\"" + pos.x + "\" cy=\"" + pos.y + "\" r=\"2\" id=\"" + (input ? "input:" : "output:")
                + label + "\" />";
        elements = elements.replaceAll("</svg>", pin + "</svg>");
        return new SVG(elements);
    }

    /**
     * Turns a XML Element to an XML String
     * @param e
     *            XML Element
     * @return XML String
     */
    private String elementToString(Element e) {
        HashSet<String> possibleRoots = new HashSet<String>();
        possibleRoots.add("g");
        possibleRoots.add("a");
        possibleRoots.add("path");
        possibleRoots.add("circle");
        possibleRoots.add("ellipse");
        possibleRoots.add("rect");
        possibleRoots.add("line");
        possibleRoots.add("polyline");
        possibleRoots.add("polygon");
        possibleRoots.add("text");
        if (!possibleRoots.contains(e.getNodeName()))
            return "";
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
