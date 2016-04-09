package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import java.awt.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * @author hneemann
 */
public abstract class SevenShape implements Shape {
    protected static final int HEIGHT = 7;
    private static final Vector ofs = new Vector(1, 12);
    private static final int LEN = 56;


    private final String label;
    protected final Style onStyle;
    protected final Style offStyle;

    public SevenShape(ElementAttributes attr) {
        this.label = attr.getLabel();
        onStyle = new Style(8, true, attr.get(AttributeKey.Color));
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
        graphic.drawLine(new Vector(th + slant, 0).add(ofs), new Vector(LEN - th + slant, 0).add(ofs), getStyle(0));
        graphic.drawLine(new Vector(LEN + slant, th - o).add(ofs), new Vector(LEN, LEN - th + o).add(ofs), getStyle(1));
        graphic.drawLine(new Vector(LEN, LEN + th - o).add(ofs), new Vector(LEN - slant, 2 * LEN - th + o).add(ofs), getStyle(2));
        graphic.drawLine(new Vector(th - slant, 2 * LEN).add(ofs), new Vector(LEN - th - slant, 2 * LEN).add(ofs), getStyle(3));
        graphic.drawLine(new Vector(0, LEN + th - o).add(ofs), new Vector(-slant, 2 * LEN - th + o).add(ofs), getStyle(4));
        graphic.drawLine(new Vector(slant, th - o).add(ofs), new Vector(0, LEN - th + o).add(ofs), getStyle(5));
        graphic.drawLine(new Vector(th, LEN).add(ofs), new Vector(LEN - th, LEN).add(ofs), getStyle(6));
        graphic.drawCircle(new Vector(LEN + dot - 1, LEN * 2 - slant - 1).add(ofs), new Vector(LEN + slant * 2 + dot + 1, LEN * 2 + slant + 1).add(ofs), getStyle(7));
    }

    protected abstract Style getStyle(int i);

}
