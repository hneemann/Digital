package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.PartDescription;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Polygon;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Pin;
import de.neemann.digital.gui.draw.parts.Pins;

/**
 * @author hneemann
 */
public class GenericShape implements Shape {
    public static final int SIZE2 = 5;
    public static final int SIZE = SIZE2 * 2;
    private final String name;
    private final int inputs;
    private final int outputs;
    private final int width;
    private final boolean symetric;
    private boolean invert = false;

    private transient Pins pins;

    public GenericShape(String name, int inputs) {
        this(name, inputs, 1);
    }

    public GenericShape(String name, int inputs, int outputs) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        width = inputs == 1 && outputs == 1 ? 1 : 3;
        symetric = outputs == 1;
    }

    public GenericShape invert(boolean invert) {
        this.invert = invert;
        return this;
    }

    @Override
    public Pins getPins(PartDescription partDescription) {
        if (pins == null) {
            ObservableValue[] outputValues = partDescription.create().getOutputs();
            String[] inputs = partDescription.getInputNames();
            pins = new Pins();

            int offs = symetric ? inputs.length / 2 * SIZE : 0;

            for (int i = 0; i < inputs.length; i++) {
                int correct = 0;
                if (symetric && ((inputs.length & 1) == 0) && i >= inputs.length / 2)
                    correct = SIZE;

                pins.add(new Pin(new Vector(0, i * SIZE + correct), inputs[i], Pin.Direction.input));
            }


            if (invert) {
                for (int i = 0; i < outputs; i++)
                    pins.add(new Pin(new Vector(SIZE * (width + 1), i * SIZE + offs), outputValues[i].getName(), Pin.Direction.output));

            } else {
                for (int i = 0; i < outputs; i++)
                    pins.add(new Pin(new Vector(SIZE * width, i * SIZE + offs), outputValues[i].getName(), Pin.Direction.output));
            }
        }
        return pins;
    }

    @Override
    public void drawTo(Graphic graphic) {
        int max = Math.max(inputs, outputs);
        int height = (max - 1) * SIZE + SIZE2;

        if (symetric && ((inputs & 1) == 0)) height += SIZE;

        graphic.drawPolygon(new Polygon(true)
                .add(1, -SIZE2)
                .add(SIZE * width - 1, -SIZE2)
                .add(SIZE * width - 1, height)
                .add(1, height), Style.NORMAL);

        if (invert) {
            int offs = symetric ? inputs / 2 * SIZE : 0;
            for (int i = 0; i < outputs; i++)
                graphic.drawCircle(new Vector(SIZE * width, i * SIZE - SIZE2 + 1 + offs),
                        new Vector(SIZE * (width + 1) - 2, i * SIZE + SIZE2 - 1 + offs), Style.NORMAL);

        }

        graphic.drawText(new Vector(SIZE2, SIZE), new Vector(SIZE, SIZE), name);
    }

}
