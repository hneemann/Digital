package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.PartDescription;
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
    public static final int SIZE2 = 5;
    public static final int SIZE = SIZE2 * 2;
    private final String name;
    private final PartDescription partDescription;
    private transient ArrayList<Pin> pins;
    private transient int max;
    private boolean invert = false;

    public GenericShape(String name, PartDescription partDescription) {
        this.name = name;
        this.partDescription = partDescription;
    }

    public GenericShape invert(boolean invert) {
        this.invert = invert;
        return this;
    }

    @Override
    public Iterable<Pin> getPins() {
        if (pins == null) {
            ObservableValue[] outputs = partDescription.create().getOutputs();
            String[] inputs = partDescription.getInputNames();
            pins = new ArrayList<>(inputs.length + outputs.length);
            for (int i = 0; i < inputs.length; i++)
                pins.add(new Pin(new Vector(0, i * SIZE), inputs[i], Pin.Direction.input));
            if (invert) {
                for (int i = 0; i < outputs.length; i++)
                    pins.add(new Pin(new Vector(SIZE * 4, i * SIZE), outputs[i].getName(), Pin.Direction.output));

            } else {
                for (int i = 0; i < outputs.length; i++)
                    pins.add(new Pin(new Vector(SIZE * 3, i * SIZE), outputs[i].getName(), Pin.Direction.output));
            }
            max = Math.max(inputs.length, outputs.length);
        }
        return pins;
    }

    @Override
    public void drawTo(Graphic graphic) {
        int height = (max - 1) * SIZE + SIZE2;
        graphic.drawPolygon(new Polygon(true)
                .add(1, -SIZE2)
                .add(SIZE * 3 - 1, -SIZE2)
                .add(SIZE * 3 - 1, height)
                .add(1, height), Style.NORMAL);

        if (invert) {
            ObservableValue[] outputs = partDescription.create().getOutputs();
            for (int i = 0; i < outputs.length; i++)
                graphic.drawCircle(new Vector(SIZE * 3, i * SIZE - SIZE2 + 1), new Vector(SIZE * 4 - 2, i * SIZE + SIZE2 - 1), Style.NORMAL);

        }

        graphic.drawText(new Vector(SIZE2, SIZE), new Vector(SIZE, SIZE), name);
    }

}
