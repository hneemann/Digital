package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.draw.elements.IOState;
import de.neemann.digital.gui.draw.elements.Pin;
import de.neemann.digital.gui.draw.elements.Pins;
import de.neemann.digital.gui.draw.graphics.*;

/**
 * Universal Shape. Used for most components.
 * Shows a simple Box with inputs at the left and outputs at the right.
 *
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
    private final String label;

    private transient Pins pins;
    private boolean showPinLabels;

    public GenericShape(String name, String[] inputs, String[] outputs) {
        this(name, inputs, outputs, null, false);
    }

    public GenericShape(String name, String[] inputs, String[] outputs, String label, boolean showPinLabels) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        if (label != null && label.length() == 0)
            label = null;
        this.label = label;
        this.showPinLabels = showPinLabels;
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
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic) {
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

        if (label != null) {
            Vector pos = new Vector(SIZE2 * width, -SIZE2 - 4);
            graphic.drawText(pos, pos.add(1, 0), label, Orientation.CENTERBOTTOM, Style.NORMAL);
        }

        if (showPinLabels) {
            for (Pin p : getPins()) {
                if (p.getDirection() == Pin.Direction.input)
                    graphic.drawText(p.getPos().add(2, 0), p.getPos().add(5, 0), p.getName(), Orientation.LEFTCENTER, Style.SHAPE_PIN);
                else
                    graphic.drawText(p.getPos().add(-2, 0), p.getPos().add(5, 0), p.getName(), Orientation.RIGHTCENTER, Style.SHAPE_PIN);
            }
        }
        if (name.length() <= 3 && !showPinLabels) {
            Vector pos = new Vector(SIZE2 * width, -SIZE2 + 2);
            graphic.drawText(pos, pos.add(1, 0), name, Orientation.CENTERTOP, Style.NORMAL);
        } else {
            Vector pos = new Vector(SIZE2 * width, height + 2);
            graphic.drawText(pos, pos.add(1, 0), name, Orientation.CENTERTOP, Style.SHAPE_PIN);
        }
    }

}
