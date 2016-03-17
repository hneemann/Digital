package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.PartDescription;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Polygon;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Pin;
import de.neemann.digital.gui.draw.parts.Pins;

import static de.neemann.digital.gui.draw.shapes.OutputShape.SIZE;

/**
 * @author hneemann
 */
public class InputShape implements Shape {

    private final int bits;

    public InputShape(int bits) {
        this.bits = bits;
    }

    @Override
    public Pins getPins(PartDescription partDescription) {
        return new Pins().add(new Pin(new Vector(0, 0), partDescription.create().getOutputs()[0].getName(), Pin.Direction.output));
    }

    @Override
    public void drawTo(Graphic graphic) {
        graphic.drawPolygon(new Polygon(true).add(-SIZE * 2 - 2, -SIZE).add(-2, -SIZE).add(-2, SIZE).add(-SIZE * 2 - 2, SIZE), Style.NORMAL);
    }
}
