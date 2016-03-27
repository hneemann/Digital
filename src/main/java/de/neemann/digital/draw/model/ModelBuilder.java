package de.neemann.digital.draw.model;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.gui.Main;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class ModelBuilder {

    private final Circuit circuit;
    private boolean disableClock = false;
    private boolean enableTrace = false;
    private ModelDescription modelDescription;

    public ModelBuilder(Circuit circuit) {
        this.circuit = circuit;
    }

    public ModelBuilder setDisableClock(boolean disableClock) {
        this.disableClock = disableClock;
        return this;
    }

    public ModelBuilder setEnableTrace(boolean enableTrace, Main main) {
        this.enableTrace = enableTrace;
        return this;
    }

    public Model build(ElementLibrary library) throws PinException, NodeException {
        modelDescription = new ModelDescription(circuit, library);
        Model model = modelDescription.createModel();
        System.out.println("build " + model.getNodes().size() + " Nodes");

        if (enableTrace) {

        }

        if (disableClock) {
            ArrayList<Clock> clocks = model.getClocks();
            if (clocks != null)
                for (Clock c : clocks)
                    c.disableTimer();
        }

        return model;
    }

    public ModelDescription getModelDescription() {
        return modelDescription;
    }
}
