package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.io.Button;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.sync.Sync;

import java.awt.*;

import static de.neemann.digital.draw.shapes.OutputShape.SIZE;

/**
 * The Button shape
 *
 * @author hneemann
 */
public class ButtonShape implements Shape {

    private static final int HEIGHT = SIZE / 2;

    private final String label;
    private final PinDescriptions outputs;
    private Button button;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public ButtonShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
        this.label = attr.getLabel();
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), outputs.get(0)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        this.button = (Button) ioState.getElement();
        ioState.getOutput(0).addObserverToValue(guiObserver);
        return new InteractorInterface() {
            @Override
            public boolean clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync) {
                return false;
            }

            @Override
            public boolean pressed(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync) {
                modelSync.access(() -> button.setPressed(true));
                return true;
            }

            @Override
            public boolean released(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync) {
                modelSync.access(() -> button.setPressed(false));
                return true;
            }

            @Override
            public boolean dragged(CircuitComponent cc, Vector pos, Transform trans, IOState ioState, Element element, Sync modelSync) {
                return false;
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic, Style heighLight) {
        boolean isPressed = false;
        if (button != null) isPressed = button.isPressed();

        if (isPressed) {
            graphic.drawPolygon(new Polygon(true)
                    .add(-SIZE * 2 - 1, -SIZE)
                    .add(-1, -SIZE)
                    .add(-1, SIZE)
                    .add(-SIZE * 2 - 1, SIZE), Style.NORMAL);
        } else {
            int t = Style.NORMAL.getThickness() / 4;
            graphic.drawPolygon(new Polygon(true)
                    .add(-SIZE * 2 - 1 - HEIGHT, -SIZE - HEIGHT)
                    .add(-1 - HEIGHT, -SIZE - HEIGHT)
                    .add(-1, -SIZE)
                    .add(-1, SIZE)
                    .add(-SIZE * 2 - 1, SIZE)
                    .add(-SIZE * 2 - 1 - HEIGHT, SIZE - HEIGHT), Style.NORMAL);
            graphic.drawPolygon(new Polygon(false)
                    .add(-1 - HEIGHT, -SIZE + t - HEIGHT)
                    .add(-1 - HEIGHT, SIZE - HEIGHT)
                    .add(t - SIZE * 2 - 1 - HEIGHT, SIZE - HEIGHT), Style.NORMAL);
            graphic.drawLine(new Vector(-1 - HEIGHT, SIZE - HEIGHT), new Vector(-1 - t, SIZE - t), Style.NORMAL);
        }

        Vector textPos = new Vector(-SIZE * 3, -4);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.RIGHTCENTER, Style.NORMAL);
    }
}
