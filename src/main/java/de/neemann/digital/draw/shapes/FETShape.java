package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.wiring.Relay;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * FET shape.
 * Created by hneemann on 25.02.17.
 */
public abstract class FETShape implements Shape {
    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private final String label;
    private Relay fet;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     * @param inputs     the inputs
     * @param outputs    the outputs
     */
    protected FETShape(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        label = attributes.getCleanLabel();
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        fet = (Relay) ioState.getElement();
        ioState.getInput(0).addObserverToValue(guiObserver);
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        final int x1 = SIZE2 - 2;
        final int g = SIZE2 / 2;
        graphic.drawPolygon(new Polygon(false)
                .add(SIZE, 0)
                .add(x1, 0)
                .add(x1, SIZE2 - g), Style.NORMAL);

        graphic.drawPolygon(new Polygon(false)
                .add(SIZE, SIZE * 2)
                .add(x1, SIZE * 2)
                .add(x1, SIZE * 2 - SIZE2 + g), Style.NORMAL);

        graphic.drawLine(new Vector(x1, SIZE2 + g), new Vector(x1, SIZE + SIZE2 - g), Style.NORMAL);

        graphic.drawLine(new Vector(1, 0), new Vector(1, SIZE * 2), Style.NORMAL);

        if (label != null && label.length() > 0)
            graphic.drawText(new Vector(SIZE + SIZE2, SIZE * 2), new Vector(SIZE * 2, SIZE * 2), label, Orientation.LEFTBOTTOM, Style.SHAPE_PIN);

        if (fet != null)
            drawSwitch(graphic);
    }

    /**
     * Draws the small switch beside the fet
     *
     * @param graphic the instance to draw to
     */
    private void drawSwitch(Graphic graphic) {
        boolean closed = fet.isClosed();
        if (closed) {
            graphic.drawLine(new Vector(SIZE + SIZE2, 0), new Vector(SIZE + SIZE2, SIZE), Style.SHAPE_PIN);
        } else {
            graphic.drawLine(new Vector(SIZE + SIZE2, 0), new Vector(SIZE + SIZE2, SIZE2 / 2), Style.SHAPE_PIN);
            graphic.drawPolygon(new Polygon(false)
                    .add(SIZE + SIZE2 / 2, SIZE2 / 2)
                    .add(SIZE + SIZE2, SIZE - SIZE2 / 2)
                    .add(SIZE + SIZE2, SIZE), Style.SHAPE_PIN);
        }
    }

    /**
     * @return the inputs (gate)
     */
    public PinDescriptions getInputs() {
        return inputs;
    }

    /**
     * @return the outputs (source and drain)
     */
    public PinDescriptions getOutputs() {
        return outputs;
    }
}
