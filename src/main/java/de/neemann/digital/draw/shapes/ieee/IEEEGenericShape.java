package de.neemann.digital.draw.shapes.ieee;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.GraphicTransform;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.shapes.GenericShape;
import de.neemann.digital.draw.shapes.InteractorInterface;
import de.neemann.digital.draw.shapes.Shape;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * IEEE Standard 91-1984 Shape
 *
 * @author hneemann
 */
public abstract class IEEEGenericShape implements Shape {

    private final PinDescription[] inputs;
    private final PinDescription[] outputs;
    private final boolean invert;

    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param inputs  inputs
     * @param outputs outputs
     * @param invert  true if NAnd, NOr
     */
    public IEEEGenericShape(PinDescription[] inputs, PinDescription[] outputs, boolean invert) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.invert = invert;
    }

    @Override
    public Pins getPins() {
        if (pins == null)
            pins = GenericShape.createPins(inputs, outputs, invert);
        return pins;
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, boolean highLight) {
        int offs = (inputs.length / 2 - 1) * SIZE;
        drawIEEE(new GraphicTransform(graphic, v -> v.add(0, offs)));

        if (offs > 0) {
            graphic.drawLine(new Vector(0, 0), new Vector(0, offs - SIZE2), Style.NORMAL);
            int h = (inputs.length / 2) * SIZE * 2;
            graphic.drawLine(new Vector(0, h), new Vector(0, h - offs + SIZE2), Style.NORMAL);
        }

        if (invert) {
            int o = inputs.length / 2 * SIZE;
            for (int i = 0; i < outputs.length; i++)
                graphic.drawCircle(new Vector(SIZE * 3 + 1, i * SIZE - SIZE2 + 1 + o),
                        new Vector(SIZE * 4 - 1, i * SIZE + SIZE2 - 1 + o), Style.NORMAL);
        }
    }

    /**
     * Draws the shape
     *
     * @param graphic the graphic instance to use
     */
    protected abstract void drawIEEE(Graphic graphic);

}
