package de.neemann.digital.draw.graphics.svg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.gui.components.graphics.svgimport.CustomVisualElement;

public class ImportSVG {

	private HashSet<String> possibleRoots = new HashSet<String>();
	private ArrayList<SVGFragment> fragments = new ArrayList<>();

	public ImportSVG(File svgFile) throws ParserConfigurationException, SAXException, IOException {
		if (!svgFile.exists())
			throw new FileNotFoundException();

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
		Document svg = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile);

		svg.getDocumentElement().normalize();
		NodeList gList;
		try {
			gList = svg.getElementsByTagName("*");
		} catch (Exception e) {
			throw new NoParsableSVGException();
		}

		for (int i = 0; i < gList.getLength(); i++) {
			if (possibleRoots.contains(gList.item(i).getNodeName())) {
				try {
					fragments.add(createElement(gList.item(i)));
				} catch (NoSuchSVGElementException e) {
				}
			}
		}
	}

	public SVGFragment createElement(Node n) throws NoSuchSVGElementException, NoParsableSVGException {
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			switch (n.getNodeName()) {
			case "path":
				return new SVGPath(((Element) n));
			case "circle":
				return new SVGCircle(((Element) n));
			case "ellipse":
				return new SVGEllipse(((Element) n));
			case "rect":
				return new SVGRectangle(((Element) n));
			case "line":
				return new SVGLine(((Element) n));
			case "polyline":
				return new SVGPolyline(((Element) n));
			case "polygon":
				return new SVGPolygon(((Element) n));
			case "text":
				return new SVGText(((Element) n));
			}
		}
		throw new NoSuchSVGElementException();
	}

	public VisualElement getElement(VisualElement proto) {
		VisualElement element = new CustomVisualElement(proto, fragments);
		return element;
	}
}
