package de.neemann.digital.draw.shapes.ieee;

import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * IEEE Standard 91-1984 And Shape
 *
 * @author hneemann
 */
public class IEEEAndShape extends IEEEGenericShape {

    private static final int STEPS = 11;
    private static final Polygon POLYGON = createPoly();

    private static Polygon createPoly() {
        Polygon p = new Polygon(true)
                .add(SIZE + SIZE2, SIZE * 2 + SIZE2)
                .add(0, SIZE * 2 + SIZE2)
                .add(0, -SIZE2)
                .add(SIZE + SIZE2, -SIZE2);

        for (int i = 1; i < STEPS; i++) {
            double w = Math.PI * i / STEPS;
            int r = SIZE2 + SIZE;
            p.add((int) (SIZE + SIZE2 + r * Math.sin(w)), (int) (SIZE - r * Math.cos(w)));
        }
        return p;
    }

    /**
     * Creates a new instance
     *
     * @param inputs  inputs
     * @param outputs outputs
     * @param invert  true if NAnd
     */
    public IEEEAndShape(PinDescription[] inputs, PinDescription[] outputs, boolean invert) {
        super(inputs, outputs, invert);
    }

    @Override
    protected void drawIEEE(Graphic graphic) {
        graphic.drawPolygon(POLYGON, Style.NORMAL);
    }

}
