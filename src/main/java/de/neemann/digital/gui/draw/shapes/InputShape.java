package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Listener;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.graphics.*;
import de.neemann.digital.gui.draw.parts.Pin;
import de.neemann.digital.gui.draw.parts.Pins;
import de.neemann.digital.gui.draw.parts.State;

import static de.neemann.digital.gui.draw.shapes.OutputShape.RAD;
import static de.neemann.digital.gui.draw.shapes.OutputShape.SIZE;

/**
 * @author hneemann
 */
public class InputShape implements Shape {

    private final int bits;
    private final String label;

    public InputShape(int bits, String label) {
        this.bits = bits;
        this.label = label;
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), "out", Pin.Direction.output));
    }

    @Override
    public Interactor applyStateMonitor(State state, Listener listener, Model model) {
        state.getOutput(0).addListener(new Listener() {
            public long lastValue;

            @Override
            public void needsUpdate() {
                long value = state.getOutput(0).getValue();
                if (lastValue != value) {
                    lastValue = value;
                    listener.needsUpdate();
                }
            }
        });
        return new Interactor() {
            @Override
            public void interact(CircuitComponent cc, Vector pos, State state) {
                long v = state.getOutput(0).getValue();
                state.getOutput(0).setValue(1 - v);
                try {
                    model.doStep();
                } catch (NodeException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic, State state) {
        Style style = Style.NORMAL;
        if (state != null) {
            if (state.getOutput(0).getValue() != 0)
                style = Style.WIRE_HIGH;
            else
                style = Style.WIRE_LOW;
        }

        Vector center = new Vector(-2 - SIZE, 0);
        graphic.drawCircle(center.sub(RAD), center.add(RAD), style);
        graphic.drawPolygon(new Polygon(true).add(-SIZE * 2 - 2, -SIZE).add(-2, -SIZE).add(-2, SIZE).add(-SIZE * 2 - 2, SIZE), Style.NORMAL);

        Vector textPos = new Vector(-SIZE * 3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.RIGHTCENTER);
    }
}
