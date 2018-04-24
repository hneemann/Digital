/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.neemann.digital.draw.graphics.Vector;

/**
 * A Group of fragments
 * @author felix
 */
public class SVGGroup implements SVGFragment, SVGPinnable {

    private ArrayList<SVGFragment> list = new ArrayList<SVGFragment>();

    /**
     * Creates a Group of fragments
     * @param n
     *            Element g or a
     * @param imp
     *            used importer
     * @throws NoParsableSVGException
     *             if the svg is not valid
     */
    public SVGGroup(Element n, ImportSVG imp) throws NoParsableSVGException {
        NodeList gList = n.getElementsByTagName("*");

        for (int i = 0; i < gList.getLength(); i++) {
            list.add(imp.createElement(gList.item(i)));
        }
    }

    @Override
    public SVGDrawable[] getDrawables() {
        ArrayList<SVGDrawable> l = new ArrayList<SVGDrawable>();
        for (SVGFragment f : list)
            if (f != null)
                for (SVGDrawable d : f.getDrawables())
                    l.add(d);
        return l.toArray(new SVGDrawable[l.size()]);
    }

    @Override
    public boolean isPin() {
        for (SVGFragment f : list) {
            if (f.isPin()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Vector getPos() {
        return null;
    }

    @Override
    public SVGPseudoPin[] getPin() {
        ArrayList<SVGPseudoPin> pins = new ArrayList<SVGPseudoPin>();
        for (SVGFragment f : list) {
            if (f != null && f.isPin())
                for (SVGPseudoPin p : ((SVGPinnable) f).getPin()) {
                    pins.add(p);
                }
        }
        return pins.toArray(new SVGPseudoPin[pins.size()]);
    }

    @Override
    public void move(Vector diff) {
        for (SVGFragment f : list) {
            f.move(diff);
        }
    }

}
