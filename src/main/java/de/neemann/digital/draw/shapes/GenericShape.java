package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;

import java.awt.*;

/**
 * Universal Shape. Used for most components.
 * Shows a simple Box with inputs at the left and outputs at the right.
 *
 * @author hneemann
 */
public class GenericShape implements Shape {
    /**
     * Half the size of the used raster
     */
    public static final int SIZE2 = 10;
    /**
     * The size of the used raster
     */
    public static final int SIZE = SIZE2 * 2;

    private final String name;
    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private final int width;
    private final boolean symmetric;
    private final String label;

    private boolean invert = false;
    private Color color = Color.WHITE;

    private transient Pins pins;
    private boolean showPinLabels;

    /**
     * Creates a new generic shape.
     *
     * @param name    the name shown in or below the shape
     * @param inputs  the used inputs
     * @param outputs the used outputs
     */
    public GenericShape(String name, PinDescriptions inputs, PinDescriptions outputs) {
        this(name, inputs, outputs, null, false);
    }

    /**
     * Creates a new generic shape.
     *
     * @param name          the name shown in or below the shape
     * @param inputs        the used inputs
     * @param outputs       the used outputs
     * @param label         the label shown above the shape
     * @param showPinLabels true if pin names visible
     */
    public GenericShape(String name, PinDescriptions inputs, PinDescriptions outputs, String label, boolean showPinLabels) {
        this(name, inputs, outputs, label, showPinLabels, inputs.size() == 1 && outputs.size() == 1 && !showPinLabels ? 1 : 3);
    }

    /**
     * Creates a new generic shape.
     *
     * @param name          the name shown in or below the shape
     * @param inputs        the used inputs
     * @param outputs       the used outputs
     * @param label         the label shown above the shape
     * @param showPinLabels true if pin names visible
     * @param width         the width of the box
     */
    public GenericShape(String name, PinDescriptions inputs, PinDescriptions outputs, String label, boolean showPinLabels, int width) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        if (label != null && label.length() == 0)
            label = null;
        this.label = label;
        this.showPinLabels = showPinLabels;
        this.width = width;
        symmetric = outputs.size() == 1;
    }

    /**
     * Sets the invert flag.
     * If set true a little circle at the putput is shown.
     *
     * @param invert true is output is inverted
     * @return this for chaind calls
     */
    public GenericShape invert(boolean invert) {
        this.invert = invert;
        return this;
    }

    /**
     * Sets the background color
     *
     * @param color the color
     * @return this for chained calls
     */
    public GenericShape setColor(Color color) {
        if (color != null)
            this.color = color;
        return this;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = createPins(inputs, outputs, invert, width, symmetric);
        }
        return pins;
    }

    /**
     * Creates pins
     *
     * @param inputs  the inputs
     * @param outputs the outputs
     * @param invert  true if invert output
     * @return the pins
     */
    public static Pins createPins(PinDescriptions inputs, PinDescriptions outputs, boolean invert) {
        return createPins(inputs, outputs, invert, 3, true);
    }

    /**
     * Creates pins
     *
     * @param inputs    the inputs
     * @param outputs   the outputs
     * @param invert    true if invert output
     * @param width     with of symbol
     * @param symmetric true if outputs in the center
     * @return the pins
     */
    public static Pins createPins(PinDescriptions inputs, PinDescriptions outputs, boolean invert, int width, boolean symmetric) {
        Pins pins = new Pins();

        int offs = symmetric ? inputs.size() / 2 * SIZE : 0;

        for (int i = 0; i < inputs.size(); i++) {
            int correct = 0;
            if (symmetric && ((inputs.size() & 1) == 0) && i >= inputs.size() / 2)
                correct = SIZE;

            pins.add(new Pin(new Vector(0, i * SIZE + correct), inputs.get(i)));
        }


        if (invert) {
            for (int i = 0; i < outputs.size(); i++)
                pins.add(new Pin(new Vector(SIZE * (width + 1), i * SIZE + offs), outputs.get(i)));

        } else {
            for (int i = 0; i < outputs.size(); i++)
                pins.add(new Pin(new Vector(SIZE * width, i * SIZE + offs), outputs.get(i)));
        }

        return pins;
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        int max = Math.max(inputs.size(), outputs.size());
        int height = (max - 1) * SIZE + SIZE2;

        if (symmetric && inputs.size() > 0 && ((inputs.size() & 1) == 0)) height += SIZE;

        Polygon polygon = new Polygon(true)
                .add(1, -SIZE2)
                .add(SIZE * width - 1, -SIZE2)
                .add(SIZE * width - 1, height)
                .add(1, height);

        if (color != Color.WHITE)
            graphic.drawPolygon(polygon, new Style(1, true, color));
        graphic.drawPolygon(polygon, Style.NORMAL);

        if (invert) {
            int offs = symmetric ? inputs.size() / 2 * SIZE : 0;
            for (int i = 0; i < outputs.size(); i++)
                graphic.drawCircle(new Vector(SIZE * width + 1, i * SIZE - SIZE2 + 1 + offs),
                        new Vector(SIZE * (width + 1) - 1, i * SIZE + SIZE2 - 1 + offs), Style.NORMAL);

        }

        if (label != null) {
            Vector pos = new Vector(SIZE2 * width, -SIZE2 - 8);
            graphic.drawText(pos, pos.add(1, 0), label, Orientation.CENTERBOTTOM, Style.NORMAL);
        }

        if (showPinLabels) {
            for (Pin p : getPins()) {
                if (p.getDirection() == Pin.Direction.input)
                    graphic.drawText(p.getPos().add(4, 0), p.getPos().add(5, 0), p.getName(), Orientation.LEFTCENTER, Style.SHAPE_PIN);
                else
                    graphic.drawText(p.getPos().add(-4, 0), p.getPos().add(5, 0), p.getName(), Orientation.RIGHTCENTER, Style.SHAPE_PIN);
            }
        }
        if (name.length() > 0) {
            if (name.length() <= 3 && !showPinLabels) {
                Vector pos = new Vector(SIZE2 * width, -SIZE2 + 4);
                graphic.drawText(pos, pos.add(1, 0), name, Orientation.CENTERTOP, Style.NORMAL);
            } else {
                Vector pos = new Vector(SIZE2 * width, height + 4);
                graphic.drawText(pos, pos.add(1, 0), name, Orientation.CENTERTOP, Style.SHAPE_PIN);
            }
        }
    }

}
