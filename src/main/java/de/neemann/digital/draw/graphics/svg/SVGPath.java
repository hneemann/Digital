package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

public class SVGPath implements SVGFragment {

	public SVGPath(Element element) throws NoParsableSVGException {
		String[] d;
		try {
			d = element.getAttribute("d").split(" ");
			for(String s:d)
			{
				System.out.println(s);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new NoParsableSVGException();
		}
	}
	
	

	@Override
	public Drawable[] getDrawables() {
		// TODO Auto-generated method stub
		return null;
	}
}
