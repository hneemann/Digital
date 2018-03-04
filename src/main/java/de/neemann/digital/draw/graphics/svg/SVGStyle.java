/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

import java.awt.Color;
import java.util.HashMap;

import de.neemann.digital.draw.graphics.Style;

/**
 * Representation of a SVG-Style
 * @author felix
 */
public class SVGStyle {
    private HashMap<String, String> attributes = new HashMap<>();

    private Color color = Color.BLACK;
    private Color fill = Color.WHITE;
    private int thickness = 1;
    private int fontSize = 14;

    /**
     * Empty Constructor
     */
    public SVGStyle() {

    }

    /**
     * Creates a SVG Style
     * @param styleString
     *            the style tag from the SVG
     */
    public SVGStyle(String styleString) {
        setStyleString(styleString);
    }

    /**
     * Sets a Stylestring
     * @param styleString
     *            stylestring
     */
    public void setStyleString(String styleString) {
        for (String s : styleString.split(";")) {
            String[] tmp = s.split(":");
            if (tmp.length > 1)
                attributes.put(tmp[0], tmp[1]);
        }
        if (attributes.containsKey("fill"))
            setFill(attributes.get("fill"));
        if (attributes.containsKey("stroke"))
            setColor(attributes.get("stroke"));
        if (attributes.containsKey("stroke-width"))
            setThickness(attributes.get("stroke-width"));
        if (attributes.containsKey("font-size"))
            setFontSize(attributes.get("font-size"));
    }

    /**
     * Set the Line-Thickness
     * @param string
     *            line thickness
     */
    public void setThickness(String string) {
        try {
            thickness = getIntFromString(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Turns a String into an integer
     * @param inp
     *            input String
     * @return corresponding int
     */
    private int getIntFromString(String inp) {
        inp = inp.replaceAll("[^0-9]", "");
        if (inp.isEmpty())
            return 0;
        return (int) Double.parseDouble(inp);
    }

    /**
     * Convert a String to an color
     * @param v
     *            colorstring
     * @return color
     */
    private Color stringToColor(String v) {
        try {
            Integer.parseInt(v, 16);
            v = "#" + v;
        } catch (Exception e) {
        }
        if (v.startsWith("#")) {
            if (v.length() == 4) {
                v = "#" + v.charAt(1) + v.charAt(1) + v.charAt(2) + v.charAt(2) + v.charAt(3) + v.charAt(3);
            }
            return Color.decode(v);
        }
        try {
            return (Color) Color.class.getField(v).get(null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return Color.WHITE;
    }

    /**
     * Sets the font size
     * @param s
     *            Size String
     */
    public void setFontSize(String s) {
        fontSize = getIntFromString(s);
    }

    /**
     * Set the Color for the Lines
     * @param v
     *            colorstring
     */
    public void setColor(String v) {
        if (!v.equals("none"))
            color = stringToColor(v);
    }

    /**
     * Set the color for the fill
     * @param v
     *            colorstring
     */
    public void setFill(String v) {
        if (!v.equals("none"))
            fill = stringToColor(v);
    }

    /**
     * Returns, whether there should be outer lines
     * @return need to draw outer lines
     */
    public boolean getShallRanded() {
        return attributes.containsKey("stroke") && !attributes.get("stroke").equals("none") || !getShallFilled();
    }

    /**
     * Returns, whether there should be a fill
     * @return need to draw fill
     */
    public boolean getShallFilled() {
        return attributes.containsKey("fill") && !attributes.get("fill").equals("none");
    }

    /**
     * Converts the Style to a drawable Style
     * @return drawable style
     */
    public Style getStyle() {
        Style s = Style.NORMAL;
        s = s.deriveStyle(thickness, false, color);
        s = s.deriveFontStyle(fontSize, true);
        return s;
    }

    /**
     * Converts the Style to a drawable inner-Style
     * @return drawable style
     */
    public Style getInnerStyle() {
        Style s = Style.NORMAL;
        s = s.deriveStyle(thickness, true, fill);
        s = s.deriveFontStyle(fontSize, true);
        return s;
    }
}
