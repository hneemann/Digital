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
    private static final Vector OFS = new Vector(1, 12);
    private static final int LEN = 56;

    private final String label;
    private final Style onStyle;
    private final Style offStyle;

    /**
     * Creates a new instance
     *
     * @param attr the attributes
     */
    public SevenShape(ElementAttributes attr) {
        this.label = attr.getLabel();
        onStyle = new Style(8, true, attr.get(Keys.COLOR));
        offStyle = new Style(8, true, new Color(230, 230, 230));
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        graphic.drawPolygon(new Polygon(true)
                .add(-SIZE2, 1)
                .add(SIZE * 3 + SIZE2, 1)
                .add(SIZE * 3 + SIZE2, HEIGHT * SIZE - 1)
                .add(-SIZE2, HEIGHT * SIZE - 1), Style.NORMAL);

        int th = onStyle.getThickness() + 1;
        int slant = 2;
        int dot = 4;
        int o = 4;
        graphic.drawLine(new Vector(th + slant, 0).add(OFS), new Vector(LEN - th + slant, 0).add(OFS), getStyleInt(0));
        graphic.drawLine(new Vector(LEN + slant, th - o).add(OFS), new Vector(LEN, LEN - th + o).add(OFS), getStyleInt(1));
        graphic.drawLine(new Vector(LEN, LEN + th - o).add(OFS), new Vector(LEN - slant, 2 * LEN - th + o).add(OFS), getStyleInt(2));
        graphic.drawLine(new Vector(th - slant, 2 * LEN).add(OFS), new Vector(LEN - th - slant, 2 * LEN).add(OFS), getStyleInt(3));
        graphic.drawLine(new Vector(0, LEN + th - o).add(OFS), new Vector(-slant, 2 * LEN - th + o).add(OFS), getStyleInt(4));
        graphic.drawLine(new Vector(slant, th - o).add(OFS), new Vector(0, LEN - th + o).add(OFS), getStyleInt(5));
        graphic.drawLine(new Vector(th, LEN).add(OFS), new Vector(LEN - th, LEN).add(OFS), getStyleInt(6));
        graphic.drawCircle(new Vector(LEN + dot - 1, LEN * 2 - slant - 1).add(OFS), new Vector(LEN + slant * 2 + dot + 1, LEN * 2 + slant + 1).add(OFS), getStyleInt(7));
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
