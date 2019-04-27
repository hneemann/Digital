/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom.svg;

import de.neemann.digital.draw.graphics.*;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.awt.*;
import java.util.HashMap;
import java.util.StringTokenizer;

class Context {
    private static final float PIXELPERMM = 72 / 25.4f * 4 / 3; // 72 DPI by definition

    private static final HashMap<String, AttrParser> PARSER = new HashMap<>();

    static {
        PARSER.put("transform", (c, value) -> c.tr = Transform.mul(new TransformParser(value).parse(), c.tr));
        PARSER.put("fill", (c, value) -> c.fill = getColorFromString(value));
        PARSER.put("fill-opacity", (c, value) -> c.fillOpacity = getFloatFromString(value));
        PARSER.put("stroke", (c, value) -> c.stroke = getColorFromString(value));
        PARSER.put("stroke-opacity", (c, value) -> c.strokeOpacity = getFloatFromString(value));
        PARSER.put("stroke-width", (c, value) -> c.thickness = getLengthFromString(c, value, 1, 1));
        PARSER.put("font-size", (c, value) -> c.fontSize = getLengthFromString(c, value, c.fontSize, c.getFontSize()));
        PARSER.put("style", Context::readStyle);
        PARSER.put("text-anchor", (c, value) -> c.textAnchor = value);
        PARSER.put("fill-rule", (c, value) -> c.fillRuleEvenOdd = value.equalsIgnoreCase("evenodd"));
        PARSER.put("class", Context::evalClass);
    }

    private final Context parent;
    private Transform tr;
    private Color fill;
    private float fillOpacity;
    private Color stroke;
    private float strokeOpacity;
    private float thickness;
    private float fontSize;
    private String textAnchor;
    private boolean fillRuleEvenOdd;
    private HashMap<String, String> classesMap;

    Context() {
        parent = null;
        tr = Transform.IDENTITY;
        thickness = 1;
        stroke = Color.BLACK;
        fontSize = Style.NORMAL.getFontSize();
        fill = Color.BLACK;
        fillOpacity = 1;
        strokeOpacity = 1;
    }

    private Context(Context parent) {
        this.parent = parent;
        tr = parent.tr;
        fill = parent.fill;
        fillOpacity = parent.fillOpacity;
        stroke = parent.stroke;
        strokeOpacity = parent.strokeOpacity;
        thickness = parent.thickness;
        fontSize = parent.fontSize;
        textAnchor = parent.textAnchor;
        fillRuleEvenOdd = parent.fillRuleEvenOdd;
    }

    Context(Context parent, Element element) throws SvgException {
        this(parent);
        final NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            final Node item = attributes.item(i);
            AttrParser p = PARSER.get(item.getNodeName());
            if (p != null)
                p.parse(this, item.getNodeValue().trim());
        }
    }

    static Context readStyle(Context context, String style) throws SvgException {
        SVGTokenizer t = new SVGTokenizer(style);
        while (true) {
            final String command = t.readTo(':');
            if (command.length() == 0)
                break;
            final String value = t.readTo(';');
            AttrParser p = PARSER.get(command);
            if (p != null)
                p.parse(context, value);
        }
        return context;
    }

    Transform getTransform() {
        return tr;
    }

    public Color getStroke() {
        return createColor(stroke, strokeOpacity);
    }

    public Color getFilled() {
        return createColor(fill, fillOpacity);
    }

    private static Color createColor(Color color, float opacity) {
        if (color == null)
            return null;
        if (opacity == 1)
            return color;
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * opacity));
    }

    public int getThickness() {
        return (int) thickness;
    }

    boolean isFillRuleEvenOdd() {
        return fillRuleEvenOdd;
    }

    Orientation getTextOrientation() {
        if (textAnchor == null)
            return Orientation.LEFTBOTTOM;

        switch (textAnchor) {
            case "end":
                return Orientation.RIGHTBOTTOM;
            case "middle":
                return Orientation.CENTERBOTTOM;
            default:
                return Orientation.LEFTBOTTOM;
        }
    }

    public VectorInterface tr(VectorInterface vector) {
        return vector.transform(tr);
    }

    public VectorInterface v(float x, float y) {
        return new VectorFloat(x, y).transform(tr);
    }

    public float getFontSize() {
        return fontSize;
    }

    void addClasses(String classes) {
        SVGTokenizer t = new SVGTokenizer(classes);
        try {
            while (t.nextIsChar('.')) {
                String key = t.readTo('{');
                String val = t.readTo('}');
                if (classesMap == null)
                    classesMap = new HashMap<>();
                classesMap.put(key, val);
            }
        } catch (SVGTokenizer.TokenizerException e) {
            // ignore errors
        }
    }

    String getCssClass(String key) {
        String v = null;
        if (classesMap != null) v = classesMap.get(key);

        if (v == null && parent != null)
            return parent.getCssClass(key);

        return v;
    }

    /**
     * Gets a length from the string
     *
     * @param value the length value, translated to pixels
     * @return the length in pixels
     */
    public float getLength(String value) {
        return getLengthFromString(this, value, 1, 0);
    }

    private static void evalClass(Context c, String value) throws SvgException {
        StringTokenizer st = new StringTokenizer(value, ", ");
        while (st.hasMoreTokens()) {
            String cl = st.nextToken();
            String style = c.getCssClass(cl);
            if (style != null)
                readStyle(c, style);
        }
    }

    private interface AttrParser {
        void parse(Context c, String value) throws SvgException;
    }

    private static Color getColorFromString(String v) {
        try {
            SVGTokenizer t = new SVGTokenizer(v);
            if (t.nextIsChar('#')) {
                String c = t.remaining();
                if (c.length() == 3)
                    return new Color(sRGB(c.charAt(0)), sRGB(c.charAt(1)), sRGB(c.charAt(2)));
                else
                    return Color.decode(v);
            } else {
                final String command = t.readCommand();
                switch (command) {
                    case "none":
                        return null;
                    case "rgb":
                        t.expect('(');
                        Color col = new Color(rgb(t), rgb(t), rgb(t));
                        t.expect(')');
                        return col;
                    default:
                        return (Color) Color.class.getField(command.toLowerCase()).get(null);
                }
            }
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    private static int rgb(SVGTokenizer t) throws SVGTokenizer.TokenizerException {
        float v = t.readFloat();
        if (t.nextIsChar('%'))
            return (int) (v * 2.55f);
        else
            return (int) v;
    }

    private static int sRGB(char c) {
        int v = Character.digit(c, 16);
        return v * 16 + v;
    }

    private static float getFloatFromString(String inp) {
        try {
            return Float.parseFloat(inp);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static float getLengthFromString(Context c, String value, float percent100, float defaultValue) {
        if (value.isEmpty())
            return defaultValue;

        SVGTokenizer t = new SVGTokenizer(value);
        try {
            float s = t.readFloat();
            if (t.isEOF())
                return s;
            else if (t.nextIsChar('%'))
                return s * percent100 / 100f;
            else {
                switch (t.readCommand()) {
                    case "em":
                        return s * c.getFontSize();
                    case "pt":
                        return 4 * s / 3;
                    case "pc":
                        return 16 * s;
                    case "px":
                        return s;
                    case "mm":
                        return s * PIXELPERMM;
                    case "cm":
                        return 10 * s * PIXELPERMM;
                    case "in":
                        return 25.4f * s * PIXELPERMM;
                    default:
                        return defaultValue;
                }
            }
        } catch (SVGTokenizer.TokenizerException e) {
            return defaultValue;
        }
    }

}
