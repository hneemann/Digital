package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.NodeList;

public class SVG {
    NodeList gList;

    public SVG() {

    }

    public SVG(NodeList gList) {
        this.gList = gList;
    }

    public NodeList getgList() {
        return gList;
    }

    public void setgList(NodeList gList) {
        this.gList = gList;
    }
}
