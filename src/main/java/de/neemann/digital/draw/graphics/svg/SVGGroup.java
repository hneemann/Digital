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
import de.neemann.digital.draw.graphics.VectorFloat;

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
        float repairX = 0;
        float repairY = 0;
        double scale = 1;
        if (!n.getAttribute("transform").isEmpty()) {
            String trans = n.getAttribute("transform");
            String[] transformations = trans.split(" ");
            for (String s : transformations) {
                String type = s.replaceAll("[^a-z]", "");
                String[] parts = s.replaceAll("[^0-9.,-]", "").split(",");
                if (type.equals("translate")) {
                    if (parts.length == 2) {
                        try {
                            repairX = Float.parseFloat(parts[0]);
                            repairY = Float.parseFloat(parts[1]);
                        } catch (Exception e) {
                            // Do nothing, if not correct
                        }
                    }
                } else if (type.equals("scale")) {
                    if (parts.length == 1) {
                        try {
                            scale = Double.parseDouble(parts[0]);
                        } catch (Exception e) {
                            // Do nothing, if not correct
                        }
                    }
                }
            }
        }
        NodeList gList = n.getElementsByTagName("*");

        for (int i = 0; i < gList.getLength(); i++) {
            list.add(imp.createElement(gList.item(i)));
        }
        VectorFloat diff = new VectorFloat(repairX, repairY).mul(-1);
        for (SVGFragment frag : list) {
            if (frag != null) {
                frag.move(diff);
                frag.scale(scale);
            }
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
    public void move(VectorFloat diff) {
        for (SVGFragment f : list) {
            if (f != null)
                f.move(diff);
        }
    }

    @Override
    public void scale(double faktor) {
        for (SVGFragment f : list) {
            if (f != null)
                f.scale(faktor);
        }
    }

}
