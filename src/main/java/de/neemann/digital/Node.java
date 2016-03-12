package de.neemann.digital;

/**
 * @author hneemann
 */
public abstract class Node implements Listener {

    private Model model;
    private int version;

    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public void needsUpdate() {
        if (model == null)
            throw new RuntimeException("noModelSet");

        if (model.getVersion() != version) {
            model.addToUpdateList(this);
            version = model.getVersion();
        }
    }

    /**
     * Only read the input!
     * It is not allowed to write to one of the outputs!!!
     *
     * @throws NodeException
     */
    public abstract void readInputs() throws NodeException;

    /**
     * Only write to the output!
     * It is not allowed to read from one of the inputs!!!
     *
     * @throws NodeException
     */
    public abstract void writeOutputs() throws NodeException;

    public void checkConsistence() throws NodeException {
    }
}
