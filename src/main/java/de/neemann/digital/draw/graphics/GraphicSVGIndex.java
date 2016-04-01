package de.neemann.digital.draw.graphics;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author hneemann
 */
public class GraphicSVGIndex extends GraphicSVG {

    public GraphicSVGIndex(File file, Vector min, Vector max) throws IOException {
        super(file, min, max);
    }

    public GraphicSVGIndex(OutputStream out, Vector min, Vector max, File source, int svgScale) throws IOException {
        super(out, min, max, source, svgScale);
    }

    @Override
    public String formatText(String text, int fontSize) {
        return formatSVGIndex(escapeXML(formatIndex(text)));
    }

    public String formatIndex(String text) {
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
