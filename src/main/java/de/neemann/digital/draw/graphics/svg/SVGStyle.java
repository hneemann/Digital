package de.neemann.digital.draw.graphics.svg;

import java.awt.Color;
import java.util.HashMap;

import de.neemann.digital.draw.graphics.Style;

public class SVGStyle {
	private HashMap<String, String> attributes = new HashMap<>();

	private Color color = Color.BLACK;
	private Color fill = Color.WHITE;
	private int thickness = 1;

	public SVGStyle(String styleString) {
		for (String s : styleString.split(";")) {
			String[] tmp = s.split(":");
			attributes.put(tmp[0], tmp[1]);
		}
		if (attributes.containsKey("fill"))
			setFill(attributes.get("fill"));
		if (attributes.containsKey("stroke"))
			setColor(attributes.get("stroke"));
		if (attributes.containsKey("stroke-width"))
			setThickness(attributes.get("stroke-width"));
	}

	private void setThickness(String string) {
		try {
			thickness = Integer.parseInt(string);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Color stringToColor(String v) {
		if (v.startsWith("#"))
			v = v.substring(1);
		try {
			Integer.parseInt(v, 16);
			return new Color(Integer.parseInt(v.substring(0, 2), 16), Integer.parseInt(v.substring(2, 4), 16),
					Integer.parseInt(v.substring(4), 16));
		} catch (Exception e) {
			return Color.getColor(v);
		}
	}

	public void setColor(String v) {
		color = stringToColor(v);
	}

	public void setFill(String v) {
		fill = stringToColor(v);
	}

	public boolean getShallRanded() {
		return attributes.containsKey("stroke");
	}

	public boolean getShallFilled() {
		return attributes.containsKey("fill") && attributes.containsKey("fill-opacity")
				? Double.parseDouble(attributes.get("fill-opacity")) >= 0.5
				: true;
	}

	public Style getStyle() {
		Style s = Style.NORMAL;
		s = s.deriveStyle(thickness, false, color);
		return s;
	}

	public Style getInnerStyle() {
		Style s = Style.NORMAL;
		s = s.deriveStyle(thickness, true, fill);
		return s;
	}
}
