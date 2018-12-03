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
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.StringTokenizer;

class Context {

    private static final HashMap<String, AttrParser> PARSER = new HashMap<>();


    static {
        PARSER.put("transform", Context::readTransform);
        PARSER.put("fill", (c, value) -> c.fill = getColorFromString(value));
        PARSER.put("fill-opacity", (c, value) -> c.fillOpacity = getFloatFromString(value));
        PARSER.put("stroke", (c, value) -> c.stroke = getColorFromString(value));
        PARSER.put("stroke-opacity", (c, value) -> c.strokeOpacity = getFloatFromString(value));
        PARSER.put("stroke-width", (c, value) -> c.thickness = getFloatFromString(value) + 1);
        PARSER.put("font-size", (c, value) -> c.fontSize = getFloatFromString(value) + 1);
        PARSER.put("style", Context::readStyle);
        PARSER.put("text-anchor", (c, value) -> c.textAnchor = value);
        PARSER.put("fill-rule", (c, value) -> c.fillRuleEvenOdd = value.equalsIgnoreCase("evenodd"));
    }

    private Transform tr;
    private Color fill;
    private float fillOpacity;
    private Color stroke;
    private float strokeOpacity;
    private float thickness;
    private float fontSize;
    private String textAnchor;
    private boolean fillRuleEvenOdd;

    Context() {
        tr = Transform.IDENTITY;
        thickness = 1;
        stroke = Color.BLACK;
        fillOpacity = 1;
        strokeOpacity = 1;
    }

    private Context(Context parent) {
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
        StringTokenizer st = new StringTokenizer(style, ";");
        while (st.hasMoreTokens()) {
            String[] t = st.nextToken().split(":");
            if (t.length == 2) {
                AttrParser p = PARSER.get(t[0].trim());
                if (p != null)
                    p.parse(context, t[1].trim());
            }
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

    public VectorInterface v(String xStr, String yStr) {
        float x = xStr.isEmpty() ? 0 : Float.parseFloat(xStr);
        float y = yStr.isEmpty() ? 0 : Float.parseFloat(yStr);
        return v(x, y);
    }

    public float getFontSize() {
        return fontSize;
    }

    private interface AttrParser {
        void parse(Context c, String value) throws SvgException;
    }

    private static void readTransform(Context c, String value) throws SvgException {
        StreamTokenizer tokenzer = new StreamTokenizer(new StringReader(value));
        try {
            Transform t;
            while (true) {
                final int tok = tokenzer.nextToken();
                if (tok == StreamTokenizer.TT_EOF)
                    break;
                if (tok != StreamTokenizer.TT_WORD)
                    throw new SvgException("invalid transform", null);
                switch (tokenzer.sval) {
                    case "translate":
                        consume(tokenzer, '(');
                        final float x = readFloat(tokenzer);
                        float y = 0;
                        if (isComma(tokenzer))
                            y = readFloat(tokenzer);
                        consume(tokenzer, ')');
                        t = new TransformTranslate(new VectorFloat(x, y));
                        break;
                    case "scale":
                        consume(tokenzer, '(');
                        final float xs = readFloat(tokenzer);
                        float ys = xs;
                        if (isComma(tokenzer))
                            ys = readFloat(tokenzer);
                        consume(tokenzer, ')');
                        t = new TransformMatrix(xs, 0, 0, ys, 0, 0);
                        break;
                    case "matrix":
                        consume(tokenzer, '(');
                        final float ma = readFloat(tokenzer);
                        consume(tokenzer, ',');
                        final float mb = readFloat(tokenzer);
                        consume(tokenzer, ',');
                        final float mc = readFloat(tokenzer);
                        consume(tokenzer, ',');
                        final float md = readFloat(tokenzer);
                        consume(tokenzer, ',');
                        final float mx = readFloat(tokenzer);
                        consume(tokenzer, ',');
                        final float my = readFloat(tokenzer);
                        consume(tokenzer, ')');
                        t = new TransformMatrix(
                                ma,
                                mc,
                                mb,
                                md,
                                mx,
                                my);
                        break;
                    case "rotate":
                        consume(tokenzer, '(');
                        float w = readFloat(tokenzer);
                        if (isComma(tokenzer)) {
                            t = TransformMatrix.rotate(w);
                            float xc = readFloat(tokenzer);
                            consume(tokenzer, ',');
                            float yc = readFloat(tokenzer);
                            t = Transform.mul(new TransformTranslate(-xc, -yc), t);
                            t = Transform.mul(t, new TransformTranslate(xc, yc));
                        } else
                            t = TransformMatrix.rotate(w);
                        consume(tokenzer, ')');
                        break;
                    default:
                        throw new SvgException("unknown transform: " + value, null);
                }
                c.tr = Transform.mul(t, c.tr);
            }
        } catch (IOException e) {
            // can never happen
        }
    }

    private static boolean isComma(StreamTokenizer tokenzer) throws IOException {
        if (tokenzer.nextToken() == ',')
            return true;
        tokenzer.pushBack();
        return false;
    }

    private static void consume(StreamTokenizer tokenzer, char c) throws IOException, SvgException {
        if (tokenzer.nextToken() != c)
            throw new SvgException("expected " + c, null);
    }

    private static float readFloat(StreamTokenizer tokenzer) throws IOException, SvgException {
        if (tokenzer.nextToken() != StreamTokenizer.TT_NUMBER)
            throw new SvgException("number expected", null);
        return (float) tokenzer.nval;
    }

    private static Color getColorFromString(String v) {
        if (v.equalsIgnoreCase("none"))
            return null;

        try {
            if (v.startsWith("#")) {
                if (v.length() == 4)
                    return new Color(sRGB(v.charAt(1)), sRGB(v.charAt(2)), sRGB(v.charAt(3)));
                else
                    return Color.decode(v);
            } else if (v.startsWith("rgb(")) {
                StringTokenizer st = new StringTokenizer(v.substring(4), " ,)");
                return new Color(rgb(st.nextToken()), rgb(st.nextToken()), rgb(st.nextToken()));
            }

            return (Color) Color.class.getField(v.toLowerCase()).get(null);
        } catch (RuntimeException | IllegalAccessException | NoSuchFieldException e) {
            return Color.BLACK;
        }
    }

    private static int rgb(String s) {
        if (s.endsWith("%"))
            return (int) (Float.parseFloat(s.substring(0, s.length() - 1)) / 100 * 255);
        else
            return Integer.parseInt(s);
    }

    private static int sRGB(char c) {
        int v = Character.digit(c, 16);
        return v * 16 + v;
    }

    private static float getFloatFromString(String inp) {
        inp = inp.replaceAll("[^0-9.]", "");
        if (inp.isEmpty())
            return 1;
        return Float.parseFloat(inp);
    }

}
