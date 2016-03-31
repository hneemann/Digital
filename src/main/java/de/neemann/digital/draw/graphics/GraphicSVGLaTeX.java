package de.neemann.digital.draw.graphics;

import java.io.File;
import java.io.IOException;

/**
 * @author hneemann
 */
public class GraphicSVGLaTeX extends GraphicSVG {
    public GraphicSVGLaTeX(File file, Vector min, Vector max) throws IOException {
        super(file, min, max);
    }

    @Override
    public void drawText(Vector p1, Vector p2, String text, Orientation orientation, Style style) {
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
        super.drawText(p1, p2, sb.toString(), orientation, style);
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
