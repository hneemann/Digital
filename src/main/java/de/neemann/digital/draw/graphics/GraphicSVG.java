package de.neemann.digital.draw.graphics;

import java.io.*;
import java.util.Date;

/**
 * @author hneemann
 */
public class GraphicSVG implements Graphic, Closeable {
    private static final int TEXTSIZE = 20;
    private static final int DEF_SCALE = 100;
    private final BufferedWriter w;

    public GraphicSVG(File file, Vector min, Vector max) throws IOException {
        this(file, min, max, null, DEF_SCALE);
    }

    public GraphicSVG(File file, Vector min, Vector max, File source) throws IOException {
        this(file, min, max, source, DEF_SCALE);
    }

    public GraphicSVG(File file, Vector min, Vector max, File source, int svgScale) throws IOException {
        w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<!-- Created with Digital  by H.Neemann -->\n");
        w.write("<!-- created: " + new Date() + " -->\n");
        if (source != null) {
            w.write("<!-- source: " + source.getPath() + " -->\n");
        }
        w.write("\n" +
                "<svg\n" +
                "   xmlns:svg=\"http://www.w3.org/2000/svg\"\n" +
                "   xmlns=\"http://www.w3.org/2000/svg\"\n");

        double width = (max.x - min.x) * svgScale / 900;
        double height = (max.y - min.y) * svgScale / 900;

        w.write("   width=\"" + width + "mm\"\n" +
                "   height=\"" + height + "mm\"\n" +
                "   viewBox=\"" + min.x + " " + min.y + " " + (max.x - min.x) + " " + (max.y - min.y) + "\">\n");
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
            w.write("<line x1=\"" + p1.x + "\" y1=\"" + p1.y + "\" x2=\"" + p2.x + "\" y2=\"" + p2.y + "\" stroke=\"black\" stroke-linecap=\"square\" stroke-width=\"" + getStrokeWidth(style) + "\"");
//            if (style.isDashed())
//                addStrokeDash(w, style.getDashArray());
            w.write(" />\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        try {
            w.write("<path d=\"M " + str(p.get(0)));
            for (int i = 1; i < p.size(); i++)
                w.write(" L " + str(p.get(i)));

            if (p.isClosed())
                w.write(" Z");

            w.write("\"");
//            if (style.isDashed())
//                addStrokeDash(w, style.getDashArray());
            w.write(" stroke=\"black\" stroke-width=\"" + getStrokeWidth(style) + "\" fill=\"none\"/>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double getStrokeWidth(Style style) {
        return style.getThickness() * 0.7;
    }

    @Override
    public void drawCircle(Vector p1, Vector p2, Style style) {
        try {
            Vector c = p1.add(p2).div(2);
            double r = Math.abs(p2.sub(p1).x) / 2;
            if (style.isFilled())
                w.write("<circle cx=\"" + c.x + "\" cy=\"" + c.y + "\" r=\"" + r + "\" stroke=\"black\" stroke-width=\"" + getStrokeWidth(style) + "\" fill=\"black\" />\n");
            else {
                w.write("<circle cx=\"" + c.x + "\" cy=\"" + c.y + "\" r=\"" + r + "\" stroke=\"black\" stroke-width=\"" + getStrokeWidth(style) + "\" fill=\"none\"");
//                if (style.isDashed())
//                    addStrokeDash(w, style.getDashArray());
                w.write(" />\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawText(Vector p1, Vector p2, String text, Orientation orientation, Style style) {
        if (text != null && text.length() > 0)
            try {
                text = escapeXML(text);

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
                    w.write("<text text-anchor=\"" + getAchor(orientation.getX()) + "\" x=\"" + p.x + "\" y=\"" + p.y + "\" fill=\"black\" style=\"font-size:" + style.getFontSize() + "\" transform=\"rotate(-90," + str(p1) + ")\" >" + text + "</text>\n");
                else
                    w.write("<text text-anchor=\"" + getAchor(orientation.getX()) + "\" x=\"" + p.x + "\" y=\"" + p.y + "\" fill=\"black\" style=\"font-size:" + style.getFontSize() + "\">" + text + "</text>\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private String escapeXML(String text) {
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
