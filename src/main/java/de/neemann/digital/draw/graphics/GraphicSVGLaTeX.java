package de.neemann.digital.draw.graphics;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Subclass of {@link GraphicSVG} which creates the correct SVG representation
 * of an index if used like "x_0". But the text itself is created to be interpreted
 * by LaTeX. To include such a SVG file in LaTeX Inkscape is needed to transform the SVG to PDF.
 * In this case the image itself is included as a PDF file in which all the text is missing.
 * Inkscape also creates a LaTeX overlay containing the text only. So you get best document quality:
 * All the graphics as included PDF, all the text set with LaTeX fonts matching the rest of your LaTeX document.
 * To run the transformation automatically by the LaTeX compiler see InkscapePDFLaTeX.pdf.
 *
 * @author hneemann
 * @see <a href="https://Inkscape.org">inkscape</a>
 * @see <a href="http://mirrors.ctan.org/info/svg-inkscape/InkscapePDFLaTeX.pdf">InkscapePDFLaTeX.pdf</a>
 */
public class GraphicSVGLaTeX extends GraphicSVG {
    /**
     * Creates new instance
     *
     * @param out the file
     * @throws IOException IOException
     */
    public GraphicSVGLaTeX(OutputStream out) throws IOException {
        super(out);
    }

    /**
     * Creates new instance
     *
     * @param out      the output stream to use
     * @param source   source file, only used to create a comment in the SVG file
     * @param svgScale the scaling
     * @throws IOException IOException
     */
    public GraphicSVGLaTeX(OutputStream out, File source, int svgScale) throws IOException {
        super(out, source, svgScale);
    }

    @Override
    public String formatText(String text, int fontSize) {
        text = formatIndex(text);
        StringBuilder sb = new StringBuilder();
        boolean inMath = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '~':
                case '\u00AC':
                    sb.append(checkMath(inMath, "\\neg{}"));
                    break;
                case '\u2265':
                    sb.append(checkMath(inMath, "\\geq\\!\\!{}"));
                    break;
                case '<':
                    if (inMath)
                        sb.append(c);
                    else
                        sb.append("\\textless{}");
                    break;
                case '>':
                    if (inMath)
                        sb.append(c);
                    else
                        sb.append("\\textgreater{}");
                    break;
                case '&':
                    sb.append("\\&");
                    break;
                case '$':
                    inMath = !inMath;
                default:
                    sb.append(c);
            }
        }
        text = sb.toString();
        if (fontSize < Style.NORMAL.getFontSize())
            text = "{\\scriptsize " + text + "}";
        return escapeXML(text);
    }

    private String checkMath(boolean inMath, String s) {
        if (inMath)
            return s;
        else
            return "$" + s + "$";
    }

    private String formatIndex(String text) {
        int p = text.lastIndexOf("_");
        if (p > 0) {
            text = "$" + text.substring(0, p) + "_{" + text.substring(p + 1) + "}$";
        }
        return text;
    }


    @Override
    public void drawCircle(Vector p1, Vector p2, Style style) {
        if ((style != Style.WIRE && style != Style.WIRE_OUT) || Math.abs(p1.x - p2.x) > 4)
            super.drawCircle(p1, p2, style);
    }

    @Override
    public String getColor(Style style) {
        if (style == Style.WIRE) return super.getColor(Style.NORMAL);
        else if (style == Style.WIRE_OUT) return super.getColor(Style.NORMAL);
        else if (style == Style.WIRE_BITS) return super.getColor(Style.NORMAL);
        else if (style == Style.WIRE_BUS) return super.getColor(Style.NORMAL);
        else if (style == Style.SHAPE_PIN) return super.getColor(Style.NORMAL);
        else return super.getColor(style);
    }
}
