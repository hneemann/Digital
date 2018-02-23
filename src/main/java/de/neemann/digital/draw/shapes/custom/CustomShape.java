package de.neemann.digital.draw.shapes.custom;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.InteractorInterface;
import de.neemann.digital.draw.shapes.Shape;

/**
 * Represents a custom shape.
 */
public class CustomShape implements Shape {
    private final CustomShapeDescription shapeDescription;
    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private Pins pins;

    /**
     * Creates a new instance
     *
     * @param shapeDescription the description of the shape
     * @param inputs           the inputs of the component
     * @param outputs          the inputs of the component
     * @throws PinException thrown if a pin is not found
     */
    public CustomShape(CustomShapeDescription shapeDescription, PinDescriptions inputs, PinDescriptions outputs) throws PinException {
        this.shapeDescription = shapeDescription;
        this.inputs = inputs;
        this.outputs = outputs;

        initPins();
    }

    private void initPins() throws PinException {
        pins = new Pins();
        for (PinDescription p : outputs)
            pins.add(new Pin(shapeDescription.getPinPos(p.getName()), p));
        for (PinDescription p : inputs)
            pins.add(new Pin(shapeDescription.getPinPos(p.getName()), p));
    }

    @Override
    public Pins getPins() {
        return pins;
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        for (Drawable d : shapeDescription)
            d.drawTo(graphic, highLight);
    }
}
