package de.neemann.digital.draw.graphics.svg;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

public class SVGGraphics implements Graphic{

	@Override
	public void drawLine(Vector p1, Vector p2, Style style) {
		System.out.println("Draw line from "+p1+" to "+p2);
	}

	@Override
	public void drawPolygon(Polygon p, Style style) {
		System.out.println("Draw "+p);
	}

	@Override
	public void drawCircle(Vector p1, Vector p2, Style style) {
		System.out.println("Draw Circle between "+p1+" and "+p2);
	}

	@Override
	public void drawText(Vector p1, Vector p2, String text, Orientation orientation, Style style) {
		System.out.println("Draw Text "+text);
	}

}
