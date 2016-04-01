package de.neemann.digital.draw.graphics;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author hneemann
 */
public class GraphicSVGLaTeX extends GraphicSVG {
    public GraphicSVGLaTeX(File file, Vector min, Vector max) throws IOException {
        super(file, min, max);
    }

    public GraphicSVGLaTeX(OutputStream out, Vector min, Vector max, File source, int svgScale) throws IOException {
        super(out, min, max, source, svgScale);
    }

    @Override
    public String formatText(String text, int fontSize) {
        text = formatIndex(text);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '~':
                case '\u00AC':
                    sb.append("$\\neg$");
                    break;
                case '\u2265':
                    sb.append("$\\geq$");
                    break;
                case '&':
                    sb.append("\\&");
                    break;
                default:
                    sb.append(c);
            }
        }
        text = sb.toString();
        if (fontSize < Style.NORMAL.getFontSize())
            text = "{\\scriptsize " + text + "}";
        return escapeXML(text);
    }

    public String formatIndex(String text) {
        int p = text.lastIndexOf("_");
        if (p > 0) {
            text = "$" + text.substring(0, p) + "_{" + text.substring(p + 1) + "}$";
        }
        return text;
    }


    @Override
    public void drawCircle(Vector p1, Vector p2, Style style) {
        if ((style != Style.WIRE && style != Style.WIRE_OUT) || Math.abs(p1.x - p2.x) > 2)
            super.drawCircle(p1, p2, style);
    }

    @Override
    public String getColor(Style style) {
        if (style == Style.WIRE) return super.getColor(Style.NORMAL);
        if (style == Style.WIRE_OUT) return super.getColor(Style.NORMAL);
        return super.getColor(style);
    }
}
