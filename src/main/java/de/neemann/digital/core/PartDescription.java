package de.neemann.digital.core;

import de.neemann.digital.gui.draw.shapes.Shape;

/**
 * @author hneemann
 */
public class PartDescription implements PartFactory {

    private final Shape shape;
    private final PartFactory creator;
    private final String[] inputNames;

    public PartDescription(Shape shape, PartFactory creator, String... inputNames) {
        this.shape = shape;
        this.creator = creator;
        this.inputNames = inputNames;
    }

    public String[] getInputNames() {
        return inputNames;
    }

    public Shape getShape() {
        return shape;
    }

    @Override
    public Part create() {
        return creator.create();
    }
}
