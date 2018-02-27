package de.neemann.digital.draw.graphics.svg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.draw.shapes.custom.CustomShapeDrawer;

/**
 * Main class for the SVG Import
 * @author felix
 */
public class ImportSVG {
    private ArrayList<SVGFragment> fragments = new ArrayList<>();
    private PinDescriptions inputs;
    private PinDescriptions outputs;
    private Pins pins = new Pins();
    private ArrayList<SVGPseudoPin> pseudoPins = new ArrayList<SVGPseudoPin>();

    /**
     * Imports a given SVG OutputPins
     * @param svgFile
     *            File to parse
     * @throws NoParsableSVGException
     *             if the SVG is corrupt
     * @throws IOException
     *             if the SVG File does not exists
     */
    public ImportSVG(File svgFile) throws NoParsableSVGException, IOException {
        if (!svgFile.exists())
            throw new FileNotFoundException();
        Document svg;
        try {
            svg = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }

        ArrayList<Element> elements = getElements(svg);
        imp(elements);
    }

    /**
     * Turns a Document in a List of Elements
     * @param svg
     *            SVG Document
     * @return List of Elements
     * @throws NoParsableSVGException
     *             if the Doc is not valid
     */
    public static ArrayList<Element> getElements(Document svg) throws NoParsableSVGException {
        svg.getDocumentElement().normalize();
        NodeList gList;
        try {
            gList = svg.getElementsByTagName("svg").item(0).getChildNodes();
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
        ArrayList<Element> ret = new ArrayList<Element>();
        for (int i = 0; i < gList.getLength(); i++) {
            if (gList.item(i) instanceof Element) {
                ret.add((Element) gList.item(i));
            }
        }
        return ret;
    }

    /**
     * Creates a List of SVGFragments from a list of XML Elements
     * @param list
     *            list of XML Elements
     * @throws NoParsableSVGException
     *             if the SVG is not Valid
     */
    private void imp(ArrayList<Element> list) throws NoParsableSVGException {
        for (Element el : list) {
            fragments.add(createElement(el));
        }
        if (inputs != null && outputs != null) {
            setPinDescriptions(inputs, outputs);
        }
    }

    /**
     * Sets the PinDescriptions
     * @param inputs
     *            Inputpins
     * @param outputs
     *            Outputpins
     * @throws NoParsableSVGException
     *             if the SVG is not valid
     */
    public void setPinDescriptions(PinDescriptions inputs, PinDescriptions outputs) throws NoParsableSVGException {
        for (SVGPseudoPin pin : pseudoPins) {
            if (pin.isInput())
                pin.setPinDesc(inputs);
            else
                pin.setPinDesc(outputs);
        }
    }

    /**
     * Gets a created SVG Object
     * @return SVG
     */
    public CustomShapeDescription getSVG() {
        CustomShapeDrawer drawer = new CustomShapeDrawer();
        for (SVGFragment f : fragments)
            if (f != null)
                for (SVGDrawable d : f.getDrawables())
                    if (d != null)
                        d.draw(drawer);
        for(SVGPseudoPin p : getPseudoPins())
            drawer.addPin(p);
        return drawer.getSvg();
    }

    /**
     * Creates a SVGFragment from a XML Node
     * @param n
     *            Node to parse
     * @return SVGFragment
     * @throws NoSuchSVGElementException
     *             if the node is not valid
     * @throws NoParsableSVGException
     *             if the svg is not parsable
     */
    public SVGFragment createElement(Node n) throws NoParsableSVGException {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            switch (n.getNodeName()) {
            case "path":
                return new SVGPath(((Element) n));
            case "circle":
            case "ellipse":
                return new SVGEllipse(((Element) n), pins, pseudoPins);
            case "rect":
                return new SVGRectangle(((Element) n));
            case "line":
                return new SVGLine(((Element) n));
            case "polyline":
                return new SVGPolygon(((Element) n), false);
            case "polygon":
                return new SVGPolygon(((Element) n));
            case "text":
                return new SVGText(((Element) n));
            case "a":
            case "g":
                return new SVGGroup((Element) n, this);
            }
        }
        return null;
    }

    /**
     * Gives the fragments of the SVG
     * @return list of fragments
     */
    public ArrayList<SVGFragment> getFragments() {
        return fragments;
    }

    /**
     * Gives the Pins of the Shape in the SVG
     * @return Pins
     */
    public Pins getPins() {
        return pins;
    }

    /**
     * get the pseudopins
     * @return Pseudopins
     */
    public ArrayList<SVGPseudoPin> getPseudoPins() {
        return pseudoPins;
    }
}
