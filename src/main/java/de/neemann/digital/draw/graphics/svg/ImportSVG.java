package de.neemann.digital.draw.graphics.svg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ImportSVG {

	private String[] possibleRoots = { "g", "a", "path", "circle", "ellipse", "rect", "line", "polyline", "polygon",
			"text" };
	private ArrayList<SVGFragment> fragments = new ArrayList<>();
	
	public ImportSVG(File svgFile) throws ParserConfigurationException, SAXException, IOException {
		if (!svgFile.exists())
			throw new FileNotFoundException();
		Document svg = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile);

		svg.getDocumentElement().normalize();
		for (String s : possibleRoots) {
			NodeList gList = svg.getElementsByTagName(s);
			for (int i = 0; i < gList.getLength(); i++) {
				try {
					fragments.add(createElement(gList.item(i)));
				} catch (NoSuchSVGElementException e) {
					System.out.println("Irrelevantes Element ignoriert");
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
			default:
				NodeList drawList = n.getChildNodes();
				for (int j = 0; j < drawList.getLength(); j++) {
					fragments.add(createElement(drawList.item(j)));
				}
			}
		}
		throw new NoSuchSVGElementException();
	}
	
	public ArrayList<SVGFragment> getFragments(){
		return fragments;
	}
}
