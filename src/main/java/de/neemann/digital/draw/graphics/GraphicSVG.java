/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import de.neemann.digital.core.element.ElementAttributes;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;

import static de.neemann.digital.draw.graphics.GraphicSwing.getMirrorYOrientation;

/**
 * Used to create a SVG representation of the circuit.
 */
public class GraphicSVG extends Graphic {
    private static final int DEF_SCALE = 15;
    private final OutputStream out;
    private final File source;
    private final int svgScale;
    private final HashSet<Flag> flags = new HashSet<>();
    private BufferedWriter w;
    private TextStyle textStyle = new TextFormatSVG();
    private ColorStyle colorStyle = Style::getColor;

    /**
     * Creates a new instance.
     *
     * @param out the stream
     */
    public GraphicSVG(OutputStream out) {
        this(out, SVGSettings.getInstance().getAttributes());
    }

    /**
     * Creates a new instance.
     *
     * @param out the stream
     * @param a   the attributes
     */
    public GraphicSVG(OutputStream out, ElementAttributes a) {
        this(out, null, DEF_SCALE);
        if (a.get(SVGSettings.LATEX))
            setTextStyle(new TextFormatLaTeX(a.get(SVGSettings.PINS_IN_MATH_MODE)));
        if (a.get(SVGSettings.HIGH_CONTRAST))
            setColorStyle(new ColorStyleHighContrast());
        if (a.get(SVGSettings.SMALL_IO))
            setFlag(Flag.smallIO);
        if (a.get(SVGSettings.HIDE_TEST))
            setFlag(Flag.hideTest);
        if (a.get(SVGSettings.NO_SHAPE_FILLING))
            setFlag(Flag.noShapeFilling);
        if (a.get(SVGSettings.NO_PIN_MARKER))
            setFlag(Flag.noPinMarker);
        if (a.get(SVGSettings.THINNER_LINES))
            setFlag(Flag.thinnerLines);

        if (a.get(SVGSettings.MONOCHROME))
            setColorStyle(new ColorStyleMonochrome(colorStyle));
    }

    /**
     * Creates a new instance.
     *
     * @param file     the file
     * @param source   source file, only used to create a comment in the SVG file
     * @param svgScale the scaling
     * @throws IOException IOException
     */
    public GraphicSVG(File file, File source, int svgScale) throws IOException {
        this(new FileOutputStream(file), source, svgScale);
    }

    /**
     * Creates a new instance.
     *
     * @param out      the stream to write the file to
     * @param source   source file, only used to create a comment in the SVG file
     * @param svgScale the scaling
     */
    public GraphicSVG(OutputStream out, File source, int svgScale) {
        this.out = out;
        this.source = source;
        this.svgScale = svgScale;
    }

    @Override
    public Graphic setBoundingBox(VectorInterface min, VectorInterface max) {
        try {
            w = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            w.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                    + "<!-- Created with Digital by H.Neemann -->\n");
            w.write("<!-- created: " + new Date() + " -->\n");
            if (source != null) {
                w.write("<!-- source: " + source.getPath() + " -->\n");
            }
            w.write("\n"
                    + "<svg\n"
                    + "   xmlns:svg=\"http://www.w3.org/2000/svg\"\n"
                    + "   xmlns=\"http://www.w3.org/2000/svg\"\n");
            double width = (max.getXFloat() - min.getXFloat() + Style.MAXLINETHICK) * svgScale / 100.0;
            double height = (max.getYFloat() - min.getYFloat() + Style.MAXLINETHICK) * svgScale / 100.0;

            final int lineCorr = Style.MAXLINETHICK / 2;

            w.write("   width=\"" + width + "mm\"\n"
                    + "   height=\"" + height + "mm\"\n"
                    + "   viewBox=\"" + (min.getX() - lineCorr)
                    + " " + (min.getY() - lineCorr)
                    + " " + (max.getX() - min.getX() + Style.MAXLINETHICK)
                    + " " + (max.getY() - min.getY() + Style.MAXLINETHICK) + "\">\n");
            w.write("<g stroke-linecap=\"square\">\n");
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (w != null) {
            w.write("</g>\n");
            w.write("</svg>");
            w.close();
        }
    }

    @Override
    public void drawLine(VectorInterface p1, VectorInterface p2, Style style) {
        try {
            w.write("<line x1=\"" + p1.getXFloat() + "\" y1=\"" + p1.getYFloat()
                    + "\" x2=\"" + p2.getXFloat() + "\" y2=\"" + p2.getYFloat()
                    + "\" stroke=\"" + getColor(style) + "\" stroke-linecap=\"square\" stroke-width=\"" + getStrokeWidth(style) + "\"");
            addStrokeDash(w, style.getDash());
            w.write(" />\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        try {
            w.write("<path d=\"" + p + "\"");
            addStrokeDash(w, style.getDash());
            if (p.getEvenOdd() && style.isFilled())
                w.write(" fill-rule=\"evenodd\"");

            if (style.isFilled() && p.isClosed() && !isFlagSet(Flag.noShapeFilling))
                w.write(" stroke=\"" + getColor(style) + "\" stroke-width=\"" + getStrokeWidth(style) + "\" fill=\"" + getColor(style) + "\" fill-opacity=\"" + getOpacity(style) + "\"/>\n");
            else {
                double strokeWidth = getStrokeWidth(style);
                if (strokeWidth == 0)
                    strokeWidth = getStrokeWidth(Style.THIN);
                w.write(" stroke=\"" + getColor(style) + "\" stroke-width=\"" + strokeWidth + "\" fill=\"none\"/>\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double getStrokeWidth(Style style) {
        if (isFlagSet(Flag.thinnerLines))
            return style.getThickness() * 0.7;
        else
            return style.getThickness();
    }

    @Override
    public void drawCircle(VectorInterface p1, VectorInterface p2, Style style) {
        try {
            VectorInterface c = p1.add(p2).div(2);
            double r = Math.abs(p2.sub(p1).getXFloat()) / 2.0;
            if (style.isFilled())
                w.write("<circle cx=\"" + c.getXFloat() + "\" cy=\"" + c.getYFloat() + "\" r=\"" + r + "\" stroke=\"" + getColor(style) + "\" stroke-width=\"" + getStrokeWidth(style) + "\" fill=\"" + getColor(style) + "\" />\n");
            else {
                w.write("<circle cx=\"" + c.getXFloat() + "\" cy=\"" + c.getYFloat() + "\" r=\"" + r + "\" stroke=\"" + getColor(style) + "\" stroke-width=\"" + getStrokeWidth(style) + "\" fill=\"none\"");
                addStrokeDash(w, style.getDash());
                w.write(" />\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void drawText(VectorInterface p1, VectorInterface p2, VectorInterface p3, String text, Orientation orientation, Style style) {
        if (text == null || text.length() == 0) return;

        try {
            text = textStyle.format(text, style);

            boolean rotateText = false;
            if (p1.getY() == p2.getY()) {   // 0 and 180 deg
                if (p1.getX() > p2.getX())   // 180
                    orientation = orientation.rot(2);
            } else {
                if (p1.getY() < p2.getY()) // 270
                    orientation = orientation.rot(2);
                else            // 90
                    orientation = orientation.rot(0);
                rotateText = true;
            }

            VectorFloat p = new VectorFloat(p1);
            int oy = getMirrorYOrientation(orientation, p1, p2, p3);
            switch (oy) {
                case 1:
                    p = p.add(new VectorFloat(0, style.getFontSize() / 2f - style.getFontSize() / 8f));
                    break;
                case 2:
                    p = p.add(new VectorFloat(0, style.getFontSize() * 3 / 4f));
                    break;
                case 0:
                    //p = p.add(0, -style.getFontSize() / 4);
                    break;
            }

            if (rotateText)
                w.write("<text text-anchor=\"" + getAchor(orientation.getX()) + "\" x=\"" + p.getXFloat() + "\" y=\"" + p.getYFloat() + "\" fill=\"" + getColor(style) + "\" style=\"font-size:" + style.getFontSize() + "px\" transform=\"rotate(-90," + str(p1) + ")\" >" + text + "</text>\n");
            else
                w.write("<text text-anchor=\"" + getAchor(orientation.getX()) + "\" x=\"" + p.getXFloat() + "\" y=\"" + p.getYFloat() + "\" fill=\"" + getColor(style) + "\" style=\"font-size:" + style.getFontSize() + "px\">" + text + "</text>\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the color to use from the given Style instance.
     * This instance creates the common HTML representation.
     *
     * @param style the {@link Style}
     * @return the COLOR
     */
    private String getColor(Style style) {
        return "#" + Integer.toHexString(colorStyle.getColor(style).getRGB()).substring(2);
    }

    private String getOpacity(Style style) {
        double op = style.getColor().getAlpha() / 255.0;
        return Double.toString(op);
    }


    /**
     * Escapes a given string to XML
     *
     * @param text the text to escape
     * @return the escaped text.
     */
    public static String escapeXML(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private String getAchor(int x) {
        switch (x) {
            case 1:
                return "middle";
            case 2:
                return "end";
            default:
                return "start";
        }
    }


    @Override
    public void openGroup() {
        try {
            w.write("<g>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeGroup() {
        try {
            w.write("</g>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void addStrokeDash(Writer w, float[] dashArray) throws IOException {
        if (dashArray != null) {
            w.write(" stroke-dasharray=\"");
            for (int i = 0; i < dashArray.length; i++) {
                if (i != 0) w.write(',');
                w.write(Float.toString(dashArray[i]));
            }
            w.write('"');
        }
    }

    private String str(VectorInterface p) {
        return p.getXFloat() + "," + p.getYFloat();
    }

    private void setTextStyle(TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    private void setColorStyle(ColorStyle colorStyle) {
        this.colorStyle = colorStyle;
    }

    private void setFlag(Flag flag) {
        flags.add(flag);
    }

    @Override
    public boolean isFlagSet(Flag flag) {
        return flags.contains(flag);
    }

    /**
     * Defines the text style.
     */
    public interface TextStyle {
        /**
         * Is used by drawText to format the given text to SVG.
         * This implementation only calls escapeXML(text).
         *
         * @param text  the text
         * @param style the text style
         * @return the formatted text
         */
        String format(String text, Style style);
    }

    /**
     * Defines the color style
     */
    public interface ColorStyle {
        /**
         * Returns the color to by used for the given style.
         *
         * @param style the style
         * @return the color to be used
         */
        Color getColor(Style style);
    }

}
