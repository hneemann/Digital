package de.neemann.digital.core;

/**
 * @author hneemann
 */
public abstract class PartFactory {

    private final String[] inputNames;

    public PartFactory(String... inputNames) {
        this.inputNames = inputNames;
    }

    public String[] getInputNames() {
        return inputNames;
    }

    public abstract Part create();
}
