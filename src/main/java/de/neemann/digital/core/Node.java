package de.neemann.digital.core;

/**
 * @author hneemann
 */
public abstract class Node implements Observer {

    private Model model;
    private int version;

    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public void hasChanged() {
        if (model == null)
            throw new RuntimeException("noModelSet");

        if (model.getStepCounter() != version) {
            model.addToUpdateList(this);
            version = model.getStepCounter();
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

    public void registerNodes(Model model) {
        model.add(this);
    }

}
