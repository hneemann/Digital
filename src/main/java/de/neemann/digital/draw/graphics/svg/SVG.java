package de.neemann.digital.draw.graphics.svg;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SVG {
    private String elements = "";
    // DeferredElementImpl element=new DeferredElementImpl(null, 0);

    public SVG() {

    }

    public SVG(ArrayList<Element> list) throws TransformerException {
        elements = "<root>";
        for (Element e : list) {
            String tmp = "<" + e.getNodeName() + " ";
            for (int i = 0; i < e.getAttributes().getLength(); i++) {
                tmp += " " + e.getAttributes().item(i).getNodeName() + "=";
                tmp += "\"" + e.getAttributes().item(i).getNodeValue() + "\"";
            }
            tmp += " />";
            elements += tmp;
        }
        elements += "</root>";
    }

    public boolean isSet() {
        return !elements.isEmpty();
    }

    public ArrayList<Element> getElements() throws NoParsableSVGException {
        Document svg;
        try {
            svg = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(elements.getBytes(Charset.defaultCharset())));
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
        svg.getDocumentElement().normalize();
        NodeList gList;
        try {
            gList = svg.getElementsByTagName("*");
        } catch (Exception e) {
            throw new NoParsableSVGException();
        }
        ArrayList<Element> ret = new ArrayList<Element>();
        for (int i = 0; i < gList.getLength(); i++) {
            if (!gList.item(i).getNodeName().equals("root"))
                ret.add((Element) gList.item(i));
        }
        return ret;
    }
}
