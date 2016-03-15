package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.PartFactory;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Polygon;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Pin;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class GenericShape implements Shape {
    private static final int SIZE2 = 6;
    private static final int SIZE = SIZE2 * 2;

    private final String name;
    private final PartFactory partFactory;
    private ArrayList<Pin> pins;
    private int max;

    public GenericShape(String name, PartFactory partFactory) {
        this.name = name;
        this.partFactory = partFactory;
    }

    @Override
    public Iterable<Pin> getPins() {
        if (pins == null) {
            ObservableValue[] outputs = partFactory.create().getOutputs();
            String[] inputs = partFactory.getInputNames();
            pins = new ArrayList<>(inputs.length + outputs.length);
            for (int i = 0; i < inputs.length; i++)
                pins.add(new Pin(new Vector(0, i * SIZE), inputs[i], Pin.Direction.input));
            for (int i = 0; i < outputs.length; i++)
                pins.add(new Pin(new Vector(SIZE * 3, i * SIZE), outputs[i].getName(), Pin.Direction.output));
            max = Math.max(inputs.length, outputs.length);
        }
        return pins;
    }

    @Override
    public void drawTo(Graphic graphic) {
        int height = (max - 1) * SIZE + SIZE2;
        graphic.drawPolygon(new Polygon(true).add(1, -SIZE2).add(SIZE * 3 - 1, -SIZE2).add(SIZE * 3 - 1, height).add(1, height), Style.NORMAL);
        graphic.drawText(new Vector(SIZE2, SIZE), new Vector(SIZE, SIZE), name);
    }

}
