package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

public class Rectangle extends Polygon {

	public Rectangle(Element element) {
		super(element);
		System.out.println("Rechteck");
	}

}
