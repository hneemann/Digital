package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

public class Circle extends Ellipse {

	public Circle(Element element) {
		super(element);
		System.out.println("Kreis");
	}

}
