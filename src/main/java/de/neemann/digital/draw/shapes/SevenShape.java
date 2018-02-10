package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import java.awt.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The shape to show a seven seg display.
 * The state of the different segments is requested by calling {@link SevenShape#getStyle(int)}.
 *
 * @author hneemann
 */
public abstract class SevenShape implements Shape {

    static final int HEIGHT = 7;
    private static final int TH = 4;
    private static final int LH = 55;
    private static final int LV = 55;
    private static final int X0 = 4;
    private static final int Y0 = 11;
    private static final int X1 = X0 - 2;
    private static final int Y1 = Y0 + 2;
    private static final int SL = 2;

    private static final Polygon A = new Polygon(true)
            .add(X0, Y0).add(X0 + TH, Y0 - TH)
            .add(X0 + LH - TH, Y0 - TH)
            .add(X0 + LH, Y0)
            .add(X0 + LH - TH, Y0 + TH)
            .add(X0 + TH, Y0 + TH);
    private static final Polygon G = A.transform(v -> v.add(-SL, LV + 4));
    private static final Polygon D = A.transform(v -> v.add(-SL * 2, 2 * LV + 8));
    private static final Polygon F = new Polygon(true)
            .add(X1, Y1)
            .add(X1 + TH, Y1 + TH)
            .add(X1 + TH - SL, Y1 + LV - TH)
            .add(X1 - SL, Y1 + LV)
            .add(X1 - TH - SL, Y1 + LV - TH)
            .add(X1 - TH, Y1 + TH);
    private static final Polygon B = F.transform(v -> v.add(LH + 4, 0));
    private static final Polygon C = F.transform(v -> v.add(LH + 4 - SL, LV + 4));
    private static final Polygon E = F.transform(v -> v.add(-SL, LV + 4));

    private static final Vector DOT = new Vector(X0 + LH + 4, Y0 + LV * 2 + 9);
    private static final Vector DOTPOS1 = DOT.add(-3, -3);
    private static final Vector DOTPOS2 = DOT.add(3, 3);

    private final Style onStyle;
    private final Style offStyle;

    /**
     * Creates a new instance
     *
     * @param attr the attributes
     */
    public SevenShape(ElementAttributes attr) {
        onStyle = Style.NORMAL.deriveFillStyle(attr.get(Keys.COLOR));
        offStyle = Style.NORMAL.deriveFillStyle(new Color(230, 230, 230));
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        graphic.drawPolygon(new Polygon(true)
                .add(-SIZE2, 1)
                .add(SIZE * 3 + SIZE2, 1)
                .add(SIZE * 3 + SIZE2, HEIGHT * SIZE - 1)
                .add(-SIZE2, HEIGHT * SIZE - 1), Style.NORMAL);

        graphic.drawPolygon(A, getStyleInt(0));
        graphic.drawPolygon(B, getStyleInt(1));
        graphic.drawPolygon(C, getStyleInt(2));
        graphic.drawPolygon(D, getStyleInt(3));
        graphic.drawPolygon(E, getStyleInt(4));
        graphic.drawPolygon(F, getStyleInt(5));
        graphic.drawPolygon(G, getStyleInt(6));

        graphic.drawCircle(DOTPOS1, DOTPOS2, getStyleInt(7));
    }

    private Style getStyleInt(int i) {
        if (getStyle(i))
            return onStyle;
        else
            return offStyle;
    }

    /**
     * Returns the state of the segment
     *
     * @param i the segments number
     * @return true if activated
     */
    protected abstract boolean getStyle(int i);

}
