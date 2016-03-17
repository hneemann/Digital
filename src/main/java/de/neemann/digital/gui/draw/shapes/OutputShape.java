package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.PartDescription;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Pin;
import de.neemann.digital.gui.draw.parts.Pins;

/**
 * @author hneemann
 */
public class OutputShape implements Shape {
    public static final int SIZE = 6;
    private final int bits;

    public OutputShape(int bits) {
        this.bits = bits;
    }

    @Override
    public Pins getPins(PartDescription partDescription) {
        return new Pins().add(new Pin(new Vector(0, 0), partDescription.getInputNames()[0], Pin.Direction.input));
    }

    @Override
    public void drawTo(Graphic graphic) {
        graphic.drawCircle(new Vector(2 + SIZE * 2, -SIZE), new Vector(2, SIZE), Style.NORMAL);
    }
}
