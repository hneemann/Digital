package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Graphic;

public class SVGCircle implements SVGFragment, Drawable {

	public SVGCircle(Element element) {
		System.out.println("Kreis");
	}

	@Override
	public Drawable[] getDrawables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void draw(Graphic graphic) {
		// TODO Auto-generated method stub
		
	}
}
