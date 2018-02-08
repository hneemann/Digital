package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Vector;

public class SVGPolygon implements SVGFragment, Drawable {

	private ArrayList<Vector> corners;
	private SVGStyle style;

	public SVGPolygon(Element element) {
		String[] points = element.getAttribute("points").split(" ");
		for (String s : points) {
			String[] tmp = s.split(",");
			corners.add(new Vector(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1])));
		}
	}

	public SVGPolygon(ArrayList<Vector> corners, SVGStyle style) {
		this.corners = corners;
		this.style = style;
	}

	@Override
	public Drawable[] getDrawables() {
		return new SVGPolygon[] { this };
	}

	@Override
	public void draw(Graphic graphic, Vector pos) {
		for (int i=0;i<corners.size();i++) {
			corners.set(i, corners.get(i).add(pos));
		}
		Polygon p = new Polygon(corners, true);
		if (style.getShallFilled()) {
			graphic.drawPolygon(p, style.getInnerStyle());
		}
		graphic.drawPolygon(p, style.getStyle());
	}
}
