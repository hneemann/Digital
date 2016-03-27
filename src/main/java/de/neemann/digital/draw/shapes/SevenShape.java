package de.neemann.digital.draw.shapes;

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
    private static final Vector ofs = new Vector(2, 8);
    private static final int LEN = 26;


    private final String label;
    protected final Style onStyle;
    protected final Style offStyle;

    public SevenShape(String label, Color color) {
        this.label = label;
        onStyle = new Style(4, true, color);
        offStyle = new Style(4, true, new Color(230, 230, 230));
    }

    @Override
    public void drawTo(Graphic graphic) {
        graphic.drawPolygon(new Polygon(true)
                .add(-SIZE2, 0)
                .add(SIZE * 3 + SIZE2, 0)
                .add(SIZE * 3 + SIZE2, HEIGHT * SIZE)
                .add(-SIZE2, HEIGHT * SIZE), Style.NORMAL);

        int th = onStyle.getThickness();
        int slant = 2;
        graphic.drawLine(new Vector(th + slant, 0).add(ofs), new Vector(LEN - th + slant, 0).add(ofs), getStyle(0));
        graphic.drawLine(new Vector(LEN + slant, th).add(ofs), new Vector(LEN, LEN - th).add(ofs), getStyle(1));
        graphic.drawLine(new Vector(LEN, LEN + th).add(ofs), new Vector(LEN - slant, 2 * LEN - th).add(ofs), getStyle(2));
        graphic.drawLine(new Vector(th - slant, 2 * LEN).add(ofs), new Vector(LEN - th - slant, 2 * LEN).add(ofs), getStyle(3));
        graphic.drawLine(new Vector(0, LEN + th).add(ofs), new Vector(-slant, 2 * LEN - th).add(ofs), getStyle(4));
        graphic.drawLine(new Vector(slant, th).add(ofs), new Vector(0, LEN - th).add(ofs), getStyle(5));
        graphic.drawLine(new Vector(th, LEN).add(ofs), new Vector(LEN - th, LEN).add(ofs), getStyle(6));
        graphic.drawCircle(new Vector(LEN, LEN * 2 - slant).add(ofs), new Vector(LEN + slant * 2, LEN * 2 + slant).add(ofs), getStyle(7));
    }

    protected abstract Style getStyle(int i);

}
