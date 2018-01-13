package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Group implements SVGFragment {
	private ArrayList<SVGFragment> group=new ArrayList<>();

	public Group(Node n) throws NoSuchSVGElementException, EmptySVGGroupException {
		NodeList drawList = n.getChildNodes();
		for (int j = 0; j < drawList.getLength(); j++) {
			group.add(RootElement.createElement(drawList.item(j)));
		}
		if(group.size()<1)
		{
			throw new EmptySVGGroupException();
		}
	}
}
