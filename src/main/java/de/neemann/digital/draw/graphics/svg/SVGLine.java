package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Graphic;

public class SVGLine implements SVGFragment, Drawable {

	public SVGLine(Element element) {
		System.out.println("Linie");
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
