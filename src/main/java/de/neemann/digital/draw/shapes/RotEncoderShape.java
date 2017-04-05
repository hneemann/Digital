package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.sync.Sync;

import java.awt.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;


/**
 * The rotary encoder shape
 *
 * @author hneemann
 */
public class RotEncoderShape implements Shape {
    private final String label;
    private final PinDescriptions outputs;
    private int state;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public RotEncoderShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
        this.label = attr.getLabel();
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(SIZE * 3, 0), outputs.get(0)))
                .add(new Pin(new Vector(SIZE * 3, SIZE), outputs.get(1)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        ioState.getOutput(0).addObserverToValue(guiObserver);
        return new InteractorInterface() {

            private int initialState;
            private boolean initial;

            @Override
            public boolean clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync) {
                return false;
            }

            @Override
            public boolean pressed(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync) {
                initial = true;
                return false;
            }

            @Override
            public boolean released(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync) {
                return false;
            }

            @Override
            public boolean dragged(CircuitComponent cc, Vector pos, Transform trans, IOState ioState, Element element, Sync modelSync) {
                if (ioState != null) {
                    Vector p = pos.sub(trans.transform(new Vector(SIZE2, SIZE2)));
                    final int dist = p.x * p.x + p.y * p.y;
                    if (dist > 100 && dist < 900) {
                        int s = (int) (Math.atan2(p.y, p.x) / Math.PI * 16);
                        if (initial) {
                            initialState = s;
                            initial = false;
                        } else {
                            // somewhat unusual but ensures that every step is visible to the model.
                            int ds = 0;
                            if (s > initialState) ds = 1;
                            else if (s < initialState) ds = -1;
                            initialState = s;
                            if (ds != 0) {
                                state += ds;
                                modelSync.access(() -> {
                                    boolean a = ((state / 2) & 1) != 0;
                                    boolean b = (((state + 1) / 2) & 1) != 0;
                                    ioState.getOutput(0).setBool(a);
                                    ioState.getOutput(1).setBool(b);
                                });
                                return true;
                            }
                        }
                    } else
                        initial = true;
                }
                return false;
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic, boolean heighLight) {
        graphic.drawPolygon(new Polygon(true)
                .add(SIZE * 3, -SIZE)
                .add(SIZE * 3, SIZE * 2)
                .add(-SIZE, SIZE * 2)
                .add(-SIZE, -SIZE), Style.NORMAL);

        graphic.drawCircle(new Vector(-SIZE, -SIZE), new Vector(SIZE * 2, SIZE * 2), Style.NORMAL);
        graphic.drawCircle(new Vector(-SIZE2, -SIZE2), new Vector(SIZE + SIZE2, SIZE + SIZE2), Style.THIN);

        final double alpha = state / 16.0 * Math.PI;
        int x = (int) ((SIZE + 1) * Math.cos(alpha));
        int y = (int) ((SIZE + 1) * Math.sin(alpha));

        graphic.drawLine(new Vector(SIZE2, SIZE2), new Vector(SIZE2 + x, SIZE2 + y), Style.NORMAL);

        Vector textPos = new Vector(SIZE, SIZE * 2 + 4);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.CENTERTOP, Style.NORMAL);
    }

}
