package de.neemann.digital.draw.graphics;

import java.io.*;
import java.util.Date;

/**
 * Used to create a SVG representation of the circuit.
 * Don't use this implementation directly. Use {@link GraphicSVGIndex} to create plain SVG or
 * {@link GraphicSVGLaTeX} if you want to include your SVG to LaTeX.
 *
 * @author hneemann
 */
public class GraphicSVG implements Graphic, Closeable {
    private static final int DEF_SCALE = 15;
    private final BufferedWriter w;

    /**
     * Creates a new instance.
     *
     * @param out the stream
     * @param min upper left corner
     * @param max lower right corner
     * @throws IOException IOException
     */
    public GraphicSVG(OutputStream out, Vector min, Vector max) throws IOException {
        this(out, min, max, null, DEF_SCALE);
    }

    /**
     * Creates a new instance.
     *
     * @param file     the file
     * @param min      upper left corner
     * @param max      lower right corner
     * @param source   source file, only used to create a comment in the SVG file
     * @param svgScale the scaling
     * @throws IOException IOException
     */
    public GraphicSVG(File file, Vector min, Vector max, File source, int svgScale) throws IOException {
        this(new FileOutputStream(file), min, max, source, svgScale);
    }

    /**
     * Creates a new instance.
     *
     * @param out      the stream to write the file to
     * @param min      upper left corner
     * @param max      lower right corner
     * @param source   source file, only used to create a comment in the SVG file
     * @param svgScale the scaling
     * @throws IOException IOException
     */
    public GraphicSVG(OutputStream out, Vector min, Vector max, File source, int svgScale) throws IOException {
        w = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
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

        double width = (max.x - min.x) * svgScale / 100.0;
        double height = (max.y - min.y) * svgScale / 100.0;

        w.write("   width=\"" + width + "mm\"\n"
                + "   height=\"" + height + "mm\"\n"
                + "   viewBox=\"" + min.x + " " + min.y + " " + (max.x - min.x) + " " + (max.y - min.y) + "\">\n");
        w.write("<g>\n");
    }

    @Override
    public void close() throws IOException {
        w.write("</g>\n");
        w.write("</svg>");
        w.close();
    }

    @Override
    public void drawLine(Vector p1, Vector p2, Style style) {
        try {
            w.write("<line x1=\"" + p1.x + "\" y1=\"" + p1.y + "\" x2=\"" + p2.x + "\" y2=\"" + p2.y + "\" stroke=\"" + getColor(style) + "\" stroke-linecap=\"square\" stroke-width=\"" + getStrokeWidth(style) + "\"");
//            if (style.isDashed())
//                addStrokeDash(w, style.getDashArray());
            w.write(" />\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        try {
            w.write("<path d=\"M " + str(p.get(0)));
            for (int i = 1; i < p.size(); i++)
                if (p.isBezierStart(i)) {
                    w.write(" C " + str(p.get(i)) + " " + str(p.get(i + 1)) + " " + str(p.get(i + 2)));
                    i += 2;
                } else
                    w.write(" L " + str(p.get(i)));

            if (p.isClosed())
                w.write(" Z");

            w.write("\"");
//            if (style.isDashed())
//                addStrokeDash(w, style.getDashArray());
            if (style.isFilled() && p.isClosed())
                w.write(" stroke=\"" + getColor(style) + "\" stroke-width=\"" + getStrokeWidth(style) + "\" fill=\"" + getColor(style) + "\"/>\n");
            else
                w.write(" stroke=\"" + getColor(style) + "\" stroke-width=\"" + getStrokeWidth(style) + "\" fill=\"none\"/>\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static double getStrokeWidth(Style style) {
        return style.getThickness() * 0.7;
    }

    @Override
    public void drawCircle(Vector p1, Vector p2, Style style) {
        try {
            Vector c = p1.add(p2).div(2);
            double r = Math.abs(p2.sub(p1).x) / 2.0;
            if (style.isFilled())
                w.write("<circle cx=\"" + c.x + "\" cy=\"" + c.y + "\" r=\"" + r + "\" stroke=\"" + getColor(style) + "\" stroke-width=\"" + getStrokeWidth(style) + "\" fill=\"" + getColor(style) + "\" />\n");
            else {
                w.write("<circle cx=\"" + c.x + "\" cy=\"" + c.y + "\" r=\"" + r + "\" stroke=\"" + getColor(style) + "\" stroke-width=\"" + getStrokeWidth(style) + "\" fill=\"none\"");
//                if (style.isDashed())
//                    addStrokeDash(w, style.getDashArray());
                w.write(" />\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void drawText(Vector p1, Vector p2, String text, Orientation orientation, Style style) {
        if (text == null || text.length() == 0) return;

        try {
            text = formatText(text, style.getFontSize());

            boolean rotateText = false;
            if (p1.y == p2.y) {   // 0 and 180 deg
                if (p1.x > p2.x)   // 180
                    orientation = orientation.rot(2);
            } else {
                if (p1.y < p2.y) // 270
                    orientation = orientation.rot(2);
                else            // 90
                    orientation = orientation.rot(0);
                rotateText = true;
            }

            Vector p = new Vector(p1);
            switch (orientation.getY()) {
                case 1:
                    p = p.add(0, style.getFontSize() / 2 - style.getFontSize() / 8);
                    break;
                case 2:
                    p = p.add(0, style.getFontSize() * 3 / 4);
                    break;
                case 0:
                    //p = p.add(0, -style.getFontSize() / 4);
                    break;
            }

            if (rotateText)
                w.write("<text text-anchor=\"" + getAchor(orientation.getX()) + "\" x=\"" + p.x + "\" y=\"" + p.y + "\" fill=\"" + getColor(style) + "\" style=\"font-size:" + style.getFontSize() + "\" transform=\"rotate(-90," + str(p1) + ")\" >" + text + "</text>\n");
            else
                w.write("<text text-anchor=\"" + getAchor(orientation.getX()) + "\" x=\"" + p.x + "\" y=\"" + p.y + "\" fill=\"" + getColor(style) + "\" style=\"font-size:" + style.getFontSize() + "\">" + text + "</text>\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Is used by drawText to format the given text to SVG.
     * This implementation only calls escapeXML(text).
     *
     * @param text     the text
     * @param fontSize the fontsize
     * @return the formated text
     */
    public String formatText(String text, int fontSize) {
        return escapeXML(text);
    }

    /**
     * Creates the color to use from the given Style instance.
     * This instance creates the common HTML representation.
     *
     * @param style the {@link Style}
     * @return the COLOR
     */
    protected String getColor(Style style) {
        return "#" + Integer.toHexString(style.getColor().getRGB()).substring(2);
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


    private static void addStrokeDash(Writer w, int[] dashArray) throws IOException {
        w.write(" stroke-dasharray=\"");
        for (int i = 0; i < dashArray.length; i++) {
            if (i != 0) w.write(',');
            w.write(Integer.toString(dashArray[i]));
        }
        w.write('"');
    }

    private String str(Vector p) {
        return p.x + "," + p.y;
    }

}
