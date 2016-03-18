package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Listener;
import de.neemann.digital.core.Model;
import de.neemann.digital.gui.draw.graphics.*;
import de.neemann.digital.gui.draw.parts.Pin;
import de.neemann.digital.gui.draw.parts.Pins;
import de.neemann.digital.gui.draw.parts.State;

/**
 * @author hneemann
 */
public class GenericShape implements Shape {
    public static final int SIZE2 = 5;
    public static final int SIZE = SIZE2 * 2;

    private final String name;
    private final String[] inputs;
    private final String[] outputs;
    private final int width;
    private final boolean symmetric;
    private boolean invert = false;

    private transient Pins pins;

    public GenericShape(String name, String[] inputs, String[] outputs) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        width = inputs.length == 1 && outputs.length == 1 ? 1 : 3;
        symmetric = outputs.length == 1;
    }

    public GenericShape invert(boolean invert) {
        this.invert = invert;
        return this;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins();

            int offs = symmetric ? inputs.length / 2 * SIZE : 0;

            for (int i = 0; i < inputs.length; i++) {
                int correct = 0;
                if (symmetric && ((inputs.length & 1) == 0) && i >= inputs.length / 2)
                    correct = SIZE;

                pins.add(new Pin(new Vector(0, i * SIZE + correct), inputs[i], Pin.Direction.input));
            }


            if (invert) {
                for (int i = 0; i < outputs.length; i++)
                    pins.add(new Pin(new Vector(SIZE * (width + 1), i * SIZE + offs), outputs[i], Pin.Direction.output));

            } else {
                for (int i = 0; i < outputs.length; i++)
                    pins.add(new Pin(new Vector(SIZE * width, i * SIZE + offs), outputs[i], Pin.Direction.output));
            }
        }
        return pins;
    }

    @Override
    public Interactor applyStateMonitor(State state, Listener listener, Model model) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, State state) {
        int max = Math.max(inputs.length, outputs.length);
        int height = (max - 1) * SIZE + SIZE2;

//        if (symmetric && state != null) {
//            graphic.drawText(new Vector(width * SIZE, 0), new Vector((width + 1) * SIZE, 0), Long.toString(state.getOutput(0).getValue()));
//        }


        if (symmetric && ((inputs.length & 1) == 0)) height += SIZE;

        graphic.drawPolygon(new Polygon(true)
                .add(1, -SIZE2)
                .add(SIZE * width - 1, -SIZE2)
                .add(SIZE * width - 1, height)
                .add(1, height), Style.NORMAL);

        if (invert) {
            int offs = symmetric ? inputs.length / 2 * SIZE : 0;
            for (int i = 0; i < outputs.length; i++)
                graphic.drawCircle(new Vector(SIZE * width, i * SIZE - SIZE2 + 1 + offs),
                        new Vector(SIZE * (width + 1) - 2, i * SIZE + SIZE2 - 1 + offs), Style.NORMAL);

        }

        graphic.drawText(new Vector(SIZE2, SIZE), new Vector(SIZE, SIZE), name, Orientation.LEFTBOTTOM);
    }

}
