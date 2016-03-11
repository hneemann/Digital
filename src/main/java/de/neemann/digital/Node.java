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
    public void needsUpdate() throws NodeException {
        if (model == null)
            throw new NodeException("no model set");

        if (model.getVersion() != version) {
            model.addToUpdateList(this);
            version = model.getVersion();
        }
    }

    /**
     * Only read the input!
     * It is not allowed to write to the outputs!!!
     *
     * @throws NodeException
     */
    public abstract void readInputs() throws NodeException;

    /**
     * Only write to the output!
     * It is not allowed to read from the inputs!!!
     *
     * @throws NodeException
     */
    public abstract void writeOutputs() throws NodeException;

}
