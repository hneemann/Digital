package de.neemann.digital.gui.components.graphics.svgimport;

import java.util.ArrayList;

import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.svg.Drawable;
import de.neemann.digital.draw.graphics.svg.SVGFragment;

public class CustomVisualElement extends VisualElement {

	private ArrayList<SVGFragment> fragments;

	public CustomVisualElement(VisualElement proto, ArrayList<SVGFragment> fragments) {
		super(proto);
		this.fragments = fragments;
	}

	@Override
	public void drawTo(Graphic graphic, Style highLight) {
		if (fragments != null) {
			for (SVGFragment f : fragments) {
				if (f != null && f.getDrawables() != null) {
					for (Drawable d : f.getDrawables()) {
						if (d != null) {
							d.draw(graphic, getPos());
						}
					}
				}
			}
		}
	}

}
