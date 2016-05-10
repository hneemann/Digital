package de.neemann.digital.draw.shapes.ieee;

import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * IEEE Standard 91-1984 XOr Shape
 *
 * @author hneemann
 */
public class IEEEXOrShape extends IEEEGenericShape {

    private static final int STEPS = 11;
    private static final int STEPS2 = 7;
    private static final Polygon POLYGON = createPoly();
    private static final Polygon POLYGON2 = createPoly2();

    private static Polygon createPoly() {
        Polygon p = new Polygon(true);

        p.add(SIZE2, SIZE * 2 + SIZE2);
        for (int i = 1; i < STEPS; i++) {
            double w = Math.PI * i / STEPS;
            int r = SIZE2 + SIZE;
            p.add((int) (SIZE2 + SIZE2 * Math.sin(w)), (int) (SIZE + r * Math.cos(w)));
        }
        p.add(SIZE2, -SIZE2);

        for (int i = 1; i < STEPS2; i++) {
            double w = Math.PI * i / STEPS2 / 3;
            int r = SIZE * 3;
            p.add((int) (SIZE2 + r * Math.sin(w)), (int) (SIZE * 2 + SIZE2 - r * Math.cos(w)));
        }
        p.add(SIZE * 3, SIZE);
        for (int i = STEPS2 - 1; i >= 1; i--) {
            double w = Math.PI * i / STEPS2 / 3;
            int r = SIZE * 3;
            p.add((int) (SIZE2 + r * Math.sin(w)), (int) (-SIZE2 + r * Math.cos(w)));
        }

        return p;
    }

    private static Polygon createPoly2() {
        Polygon p = new Polygon(false);

        p.add(0, SIZE * 2 + SIZE2);
        for (int i = 1; i < STEPS; i++) {
            double w = Math.PI * i / STEPS;
            int r = SIZE2 + SIZE;
            p.add((int) (SIZE2 * Math.sin(w)), (int) (SIZE + r * Math.cos(w)));
        }
        p.add(0, -SIZE2);

        return p;
    }

    /**
     * Creates a new instance
     *
     * @param inputs  inputs
     * @param outputs outputs
     * @param invert  true if XNOr
     */
    public IEEEXOrShape(PinDescription[] inputs, PinDescription[] outputs, boolean invert) {
        super(inputs, outputs, invert);
    }

    @Override
    protected void drawIEEE(Graphic graphic) {
        graphic.drawPolygon(POLYGON, Style.NORMAL);
        graphic.drawPolygon(POLYGON2, Style.NORMAL);
    }
}
