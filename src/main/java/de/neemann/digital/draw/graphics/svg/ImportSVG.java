package de.neemann.digital.draw.graphics.svg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.Pins;

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
    private SVG objSVG;

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

        svg.getDocumentElement().normalize();
        NodeList gList;
        try {
            gList = svg.getElementsByTagName("*");
        } catch (Exception e) {
            throw new NoParsableSVGException();
        }
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
        ArrayList<Element> elements = new ArrayList<Element>();
        for (int i = 0; i < gList.getLength(); i++) {
            if (possibleRoots.contains(gList.item(i).getNodeName())) {
                elements.add((Element) gList.item(i));
            }
        }
        objSVG = new SVG(elements);
        imp(elements);
    }

    /**
     * Creates a ImportSVG from a concrete svg, prefilled with Pin Descriptions
     * @param svg
     *            SVG
     * @param inputs
     *            Inputs
     * @param outputs
     *            Outputs
     * @throws NoParsableSVGException
     *             if the SVG is not valid
     */
    public ImportSVG(SVG svg, PinDescriptions inputs, PinDescriptions outputs) throws NoParsableSVGException {
        if (inputs != null && outputs != null) {
            this.inputs = inputs;
            this.outputs = outputs;
            setPinDescriptions(inputs, outputs);
        }
        imp(svg.getElements());
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
            try {
                fragments.add(createElement(el));
            } catch (NoSuchSVGElementException e) {
            }
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
    public SVG getSVG() {
        return objSVG;
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
    public SVGFragment createElement(Node n) throws NoSuchSVGElementException, NoParsableSVGException {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            switch (n.getNodeName()) {
            case "path":
                return new SVGPath(((Element) n));
            case "circle":
            case "ellipse":
                return new SVGEllipse(((Element) n), pins, inputs, outputs, pseudoPins);
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
            }
        }
        throw new NoSuchSVGElementException();
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
}
