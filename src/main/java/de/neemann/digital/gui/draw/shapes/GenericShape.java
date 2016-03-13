package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Polygon;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Pin;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class GenericShape implements Shape {

    private final String name;
    private int inputs;
    private int outputs;
    private ArrayList<Pin> pins;

    public GenericShape(String name, int inputs, int outputs) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    @Override
    public Iterable<Pin> getPins() {
        if (pins == null) {
            pins = new ArrayList<>(inputs + outputs);
            for (int i = 0; i < inputs; i++)
                pins.add(new Pin(new Vector(0, i * 5), "i" + i));
            for (int i = 0; i < outputs; i++)
                pins.add(new Pin(new Vector(15, i * 5), "o" + i));

        }
        return pins;
    }

    @Override
    public void drawTo(Graphic graphic) {
        int max = Math.max(inputs, outputs);
        int height = (max - 1) * 5 + 2;
        graphic.drawPolygon(new Polygon(true).add(1, -2).add(14, -2).add(14, height).add(1, height), Style.NORMAL);
        graphic.drawText(new Vector(3, 5), new Vector(4, 5), name);
    }

    public int getInputs() {
        return inputs;
    }

    public void setInputs(int inputs) {
        pins = null;
        this.inputs = inputs;
    }

    public int getOutputs() {
        return outputs;
    }

    public void setOutputs(int outputs) {
        pins = null;
        this.outputs = outputs;
    }
}
