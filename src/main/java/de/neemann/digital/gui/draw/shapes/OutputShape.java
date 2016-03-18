package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Listener;
import de.neemann.digital.core.Model;
import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Orientation;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Pin;
import de.neemann.digital.gui.draw.parts.Pins;
import de.neemann.digital.gui.draw.parts.State;

/**
 * @author hneemann
 */
public class OutputShape implements Shape {
    public static final int SIZE = 8;
    public static final Vector RAD = new Vector(SIZE - 3, SIZE - 3);
    public static final Vector RADL = new Vector(SIZE, SIZE);
    private final int bits;
    private final String label;

    public OutputShape(int bits, String label) {
        this.bits = bits;
        this.label = label;
    }

    @Override
    public Pins getPins() {
        return new Pins().add(new Pin(new Vector(0, 0), "in", Pin.Direction.input));
    }

    @Override
    public Interactor applyStateMonitor(State state, Listener listener, Model model) {
        state.getInput(0).addListener(new Listener() {
            public long lastValue = 0;

            @Override
            public void needsUpdate() {
                long value = state.getInput(0).getValue();
                if (lastValue != value) {
                    lastValue = value;
                    listener.needsUpdate();
                }
            }
        });
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, State state) {
        Style style = Style.NORMAL;
        if (state != null) {
            if (state.getInput(0).getValue() != 0)
                style = Style.WIRE_HIGH;
            else
                style = Style.WIRE_LOW;
        }

        Vector center = new Vector(2 + SIZE, 0);
        graphic.drawCircle(center.sub(RAD), center.add(RAD), style);
        graphic.drawCircle(center.sub(RADL), center.add(RADL), Style.NORMAL);
        Vector textPos = new Vector(SIZE * 2 + 3, 0);
        graphic.drawText(textPos, textPos.add(1, 0), label, Orientation.LEFTCENTER, Style.NORMAL);
    }
}
