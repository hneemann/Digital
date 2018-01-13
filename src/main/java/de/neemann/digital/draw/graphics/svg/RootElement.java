package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class RootElement implements SVGFragment{
	public RootElement(Element element) {
		
	}
	
	public static SVGFragment createElement(Node n) throws NoSuchSVGElementException, EmptySVGGroupException {
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			switch (n.getNodeName()) {
			case "path":
				return new Path(((Element)n));
			case "circle":
				return new Circle(((Element)n));
			case "ellipse":
				return new Ellipse(((Element)n));
			case "rect":
				return new Rectangle(((Element)n));
			case "line":
				return new Line(((Element)n));
			case "polyline":
				return new Polyline(((Element)n));
			case "polygon":
				return new Polygon(((Element)n));
			case "text":
				return new Text(((Element)n));
			default:
				return new Group(((Element)n));
			}
		}
		throw new NoSuchSVGElementException();
	}
}
