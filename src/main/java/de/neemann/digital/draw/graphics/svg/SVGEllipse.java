package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Vector;

public class SVGEllipse implements SVGFragment, Drawable {

	public SVGEllipse(Element element) {
		System.out.println("Ellipse");
	}

	@Override
	public Drawable[] getDrawables() {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public void draw(Graphic graphic, Vector pos) {
		// TODO Auto-generated method stub
		
	}
}