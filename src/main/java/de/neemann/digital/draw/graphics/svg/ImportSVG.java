package de.neemann.digital.draw.graphics.svg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ImportSVG {

	private String[] possibleRoots = { "g", "a", "path", "circle", "ellipse", "rect", "line", "polyline", "polygon",
			"text" };
	private ArrayList<SVGFragment> fragments = new ArrayList<>();

	public ImportSVG(File svgFile) throws ParserConfigurationException, SAXException, IOException {
		if (!svgFile.exists())
			throw new FileNotFoundException();
		Document svg = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile);

		svg.getDocumentElement().normalize();
		for (String s : possibleRoots) {
			NodeList gList = svg.getElementsByTagName(s);
			for (int i = 0; i < gList.getLength(); i++) {
				try {
					fragments.add(RootElement.createElement(gList.item(i)));
				} catch (NoSuchSVGElementException e) {
					System.out.println("Fehlerhaftes Element ignoriert");
				} catch (EmptySVGGroupException e) {
					System.out.println("Leere Gruppe nicht hinzugefügt");
				}
			}
		}
	}

	public static void main(String args[]) {
		try {
			new ImportSVG(new File("/home/felix/Nextcloud/Dokumente/DHBW/Studienarbeit/svgs/dialog-close.svg"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
