package de.neemann.digital.core;

/**
 * @author hneemann
 */
public class PartDescription implements PartFactory {

    private final PartFactory creator;
    private final String[] inputNames;

    public PartDescription(PartFactory creator, String... inputNames) {
        this.creator = creator;
        this.inputNames = inputNames;
    }

    public String[] getInputNames() {
        return inputNames;
    }

    @Override
    public Part create() {
        return creator.create();
    }
}
