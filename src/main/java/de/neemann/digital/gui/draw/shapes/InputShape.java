package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Listener;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.PartDescription;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Polygon;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
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

    public InputShape(int bits) {
        this.bits = bits;
    }

    @Override
    public Pins getPins(PartDescription partDescription) {
        return new Pins().add(new Pin(new Vector(0, 0), partDescription.create().getOutputs()[0].getName(), Pin.Direction.output));
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
        Style style = Style.WIRE;
        if (state != null && state.getOutput(0).getValue() != 0)
            style = Style.WIRE_HIGH;

        Vector center = new Vector(-2 - SIZE, 0);
        graphic.drawCircle(center.sub(RAD), center.add(RAD), style);
        graphic.drawPolygon(new Polygon(true).add(-SIZE * 2 - 2, -SIZE).add(-2, -SIZE).add(-2, SIZE).add(-SIZE * 2 - 2, SIZE), Style.NORMAL);
    }
}
