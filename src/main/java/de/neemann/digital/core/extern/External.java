/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;

import javax.swing.*;
import java.io.IOException;

/**
 * The external component
 */
public class External extends Node implements Element {

    /**
     * The external component description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(External.class) {
        @Override
        public PinDescriptions getInputDescription(ElementAttributes elementAttributes) {
            return new PortDefinition(elementAttributes.get(Keys.EXTERNAL_INPUTS)).getPinDescriptions(PinDescription.Direction.input);
        }

        @Override
        public PinDescriptions getOutputDescriptions(ElementAttributes elementAttributes) {
            return new PortDefinition(elementAttributes.get(Keys.EXTERNAL_OUTPUTS)).getPinDescriptions(PinDescription.Direction.output);
        }

    }
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.EXTERNAL_INPUTS)
            .addAttribute(Keys.EXTERNAL_OUTPUTS)
            .addAttribute(Keys.EXTERNAL_CODE)
            .addAttribute(Keys.PROCESS_TYPE);

    private final ProcessFactory.Type type;
    private final PortDefinition ins;
    private final PortDefinition outs;
    private final ObservableValues outputs;
    private final String code;
    private final String label;
    private ObservableValues inputs;
    private ProcessHandler processHandler;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public External(ElementAttributes attr) {
        super(true);
        ins = new PortDefinition(attr.get(Keys.EXTERNAL_INPUTS));
        outs = new PortDefinition(attr.get(Keys.EXTERNAL_OUTPUTS));
        outputs = outs.createOutputs();
        label = attr.getCleanLabel();
        type = attr.get(Keys.PROCESS_TYPE);
        code = attr.get(Keys.EXTERNAL_CODE);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        this.inputs = inputs;
        for (int i = 0; i < inputs.size(); i++)
            inputs.get(i).checkBits(ins.getPort(i).getBits(), this, i).addObserverToValue(this);
    }


    @Override
    public void readInputs() throws NodeException {
        try {
            processHandler.writeValues(inputs);
        } catch (IOException e) {
            throw new NodeException(Lang.get("err_errorWritingDataToProcess"), this, -1, inputs, e);
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        try {
            processHandler.readValues(outputs);
        } catch (IOException e) {
            throw new NodeException(Lang.get("err_errorReadingDataToProcess"), this, -1, outputs, e);
        }
    }

    @Override
    public ObservableValues getOutputs() {
        return outputs;
    }

    @Override
    public void init(Model model) throws NodeException {
        try {
            processHandler = ProcessFactory.create(type, label, code, ins, outs);
        } catch (IOException e) {
            throw new NodeException(Lang.get("err_errorCreatingProcess"), this, -1, null, e);
        }

        model.addObserver(event -> {
            if (event.equals(ModelEvent.STOPPED)) {
                try {
                    processHandler.close();
                } catch (IOException e) {
                    SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorClosingExternalProcess")).addCause(e));
                }
            }
        }, ModelEvent.STOPPED);
    }
}
