package de.neemann.digital.draw.graphics.svg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ImportSVG {
	public ImportSVG(File svgFile) throws ParserConfigurationException, SAXException, IOException {
		if (!svgFile.exists())
			throw new FileNotFoundException();
		Document svg = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile);

		svg.getDocumentElement().normalize();
		NodeList gList = svg.getElementsByTagName("g");
		for (int i = 0; i < gList.getLength(); i++) {
			NodeList drawList = gList.item(i).getChildNodes();
			for (int j = 0; j < drawList.getLength(); j++) {
				if (drawList.item(j).getNodeType() == Node.ELEMENT_NODE) {
					System.out.println(drawList.item(j).getNodeName());
					switch (drawList.item(j).getNodeName()) {
					case "path":
						drawPath(((Element)drawList.item(j)));
						break;
					default:
						// throw new NoParsableSVGException();
					}
				}
			}
		}
	}

	private void drawPath(Element element) {
		System.out.println("Zeiche Linie: " + element.getAttribute("d"));
	}

	public static void main(String args[]) {
		try {
			new ImportSVG(new File("/home/felix/Nextcloud/Dokumente/DHBW/Studienarbeit/test.svg"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
