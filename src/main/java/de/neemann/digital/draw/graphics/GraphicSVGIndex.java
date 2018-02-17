package de.neemann.digital.draw.graphics;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static de.neemann.digital.core.element.ElementAttributes.cleanLabel;

/**
 * Subclass of {@link GraphicSVG} which creates the correct SVG representation
 * of an index if used like "x_0".
 *
 * @author hneemann
 */
public class GraphicSVGIndex extends GraphicSVG {

    /**
     * Creates new instance
     *
     * @param out the file
     * @throws IOException IOException
     */
    public GraphicSVGIndex(OutputStream out) throws IOException {
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
    public GraphicSVGIndex(OutputStream out, File source, int svgScale) throws IOException {
        super(out, source, svgScale);
    }

    @Override
    public String formatText(String text, int fontSize) {
        text = text.replace('~', '\u00ac');
        return formatSVGIndex(escapeXML(formatIndex(cleanLabel(text))));
    }

    private String formatIndex(String text) {
        int p = text.lastIndexOf("_");
        if (p > 0) {
            text = text.substring(0, p) + "_{" + text.substring(p + 1) + "}";
        }
        return text;
    }


    private String formatSVGIndex(String text) {
        int p1;
        while ((p1 = text.indexOf("_{")) >= 0) {
            int p2 = text.indexOf('}', p1);
            if (p2 >= 0) {
                String ind = text.substring(p1 + 2, p2);
                if (ind.length() > 0)
                    ind = "<tspan style=\"font-size:80%;baseline-shift:sub\">" + ind + "</tspan>";
                text = text.substring(0, p1) + ind + text.substring(p2 + 1);
            }
        }
        while ((p1 = text.indexOf("^{")) >= 0) {
            int p2 = text.indexOf('}', p1);
            if (p2 >= 0) {
                String ind = text.substring(p1 + 2, p2);
                if (ind.length() > 0)
                    ind = "<tspan style=\"font-size:80%;baseline-shift:super\">" + ind + "</tspan>";
                text = text.substring(0, p1) + ind + text.substring(p2 + 1);
            }
        }
        return text;
    }
}
