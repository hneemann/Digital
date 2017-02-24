package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.wiring.NFET;
import de.neemann.digital.core.wiring.Relay;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The FETS shape
 */
public class NFETShape implements Shape {

    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private final String label;
    private NFET fet;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     * @param inputs     the inputs
     * @param outputs    the outputs
     */
    public NFETShape(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        label = attributes.getCleanLabel();
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(0, SIZE * 2), inputs.get(0)))
                .add(new Pin(new Vector(SIZE, 0), outputs.get(0)))
                .add(new Pin(new Vector(SIZE, SIZE * 2), outputs.get(1)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        fet = (NFET) ioState.getElement();
        ioState.getInput(0).addObserverToValue(guiObserver);
        ioState.getInput(2).addObserverToValue(guiObserver);
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

        graphic.drawPolygon(new Polygon(false)
                .add(SIZE2 + 3, SIZE)
                .add(SIZE, SIZE)
                .add(SIZE, SIZE * 2), Style.THIN);

        graphic.drawPolygon(new Polygon(true)
                .add(x1 + 4, SIZE)
                .add(SIZE - SIZE2 / 3, SIZE - SIZE2 / 4)
                .add(SIZE - SIZE2 / 3, SIZE + SIZE2 / 4), Style.THIN_FILLED);

        graphic.drawLine(new Vector(x1, SIZE2 + g), new Vector(x1, SIZE + SIZE2 - g), Style.NORMAL);

        graphic.drawLine(new Vector(1, 0), new Vector(1, SIZE * 2), Style.NORMAL);

        if (label != null && label.length() > 0)
            graphic.drawText(new Vector(SIZE + SIZE2, SIZE * 2), new Vector(SIZE * 2, SIZE * 2), label, Orientation.LEFTBOTTOM, Style.SHAPE_PIN);

        if (fet != null)
            drawSwitch(graphic, fet);
    }

    /**
     * Draws the small switch beside the fet
     *
     * @param graphic the instance to draw to
     * @param fet     the fet
     */
    public static void drawSwitch(Graphic graphic, Relay fet) {
        boolean closed = fet.isClosed();
        if (closed) {
            graphic.drawLine(new Vector(SIZE + SIZE2, 0), new Vector(SIZE + SIZE2, SIZE), Style.THIN);
        } else {
            graphic.drawLine(new Vector(SIZE + SIZE2, 0), new Vector(SIZE + SIZE2, SIZE2 / 2), Style.THIN);
            graphic.drawPolygon(new Polygon(false)
                    .add(SIZE + SIZE2 / 2, SIZE2 / 2)
                    .add(SIZE + SIZE2, SIZE - SIZE2 / 2)
                    .add(SIZE + SIZE2, SIZE), Style.THIN);
        }
    }

}
