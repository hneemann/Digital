package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.draw.elements.Circuit;
import de.neemann.digital.gui.draw.elements.PinException;
import de.neemann.digital.gui.draw.library.ElementLibrary;

/**
 * @author hneemann
 */
public class ModelBuilder {

    private final Circuit circuit;
    private boolean bindWiresToGui = true;
    private boolean disableClock = false;
    private boolean enableTrace = false;
    private ModelDescription modelDescription;

    public ModelBuilder(Circuit circuit) {
        this.circuit = circuit;
    }

    public ModelBuilder setBindWiresToGui(boolean bindWiresToGui) {
        this.bindWiresToGui = bindWiresToGui;
        return this;
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
        Model model = modelDescription.createModel(bindWiresToGui);

        if (enableTrace) {

        }

        if (disableClock) {
            for (Clock c : model.getClocks())
                c.disableTimer();
        }

        return model;
    }

    public ModelDescription getModelDescription() {
        return modelDescription;
    }
}
