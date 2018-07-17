/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.switching;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.bus.BusModelStateObserver;
import de.neemann.digital.core.wiring.bus.CommonBusValue;
import de.neemann.digital.lang.Lang;

/**
 * A simple switch
 */
public class Switch implements Element, NodeInterface {

    /**
     * Defines a direction for the switch. NO means no direction is given, the switch is bidirectional.
     */
    public enum Unidirectional {NO, FROM1TO2, FROM2TO1}

    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Switch.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.CLOSED);

    private final ObservableValue output1;
    private final ObservableValue output2;
    private final int bits;
    private boolean closed;
    private SwitchModel switchModel;
    private Unidirectional unidirectional = Unidirectional.NO;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public Switch(ElementAttributes attr) {
        this(attr, attr.get(Keys.CLOSED), "out1", "out2");
        output1.setPinDescription(DESCRIPTION);
        output2.setPinDescription(DESCRIPTION);
    }

    /**
     * Creates a new instance
     *
     * @param attr   the elements attributes
     * @param closed true if switch is closed
     * @param out1   name of output 1
     * @param out2   name of output 2
     */
    public Switch(ElementAttributes attr, boolean closed, String out1, String out2) {
        bits = attr.getBits();
        this.closed = closed;
        output1 = new ObservableValue(out1, bits).setBidirectional().setToHighZ();
        output2 = new ObservableValue(out2, bits).setBidirectional().setToHighZ();
    }

    /**
     * Creates a new instance
     *
     * @param closed  initial state
     * @param output1 first output
     * @param output2 second output
     */
    public Switch(ObservableValue output1, ObservableValue output2, boolean closed) {
        this.bits = output1.getBits();
        this.closed = closed;
        this.output1 = output1;
        this.output2 = output2;
    }

    /**
     * Sets this switch to unidirectional
     *
     * @param unidirectional the state
     * @return this for chained calls
     */
    public Switch setUnidirectional(Unidirectional unidirectional) {
        this.unidirectional = unidirectional;
        return this;
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        ObservableValue input1 = inputs.get(0).addObserverToValue(this).checkBits(bits, null);
        ObservableValue input2 = inputs.get(1).addObserverToValue(this).checkBits(bits, null);
        switch (unidirectional) {
            case NO:
                if (input1 instanceof CommonBusValue) {
                    if (input2 instanceof CommonBusValue) {
                        final CommonBusValue in1 = (CommonBusValue) input1;
                        final CommonBusValue in2 = (CommonBusValue) input2;
                        ObservableValue constant = in1.searchConstant();
                        if (constant != null)
                            switchModel = new UniDirectionalSwitch(constant, output2);
                        else {
                            constant = in2.searchConstant();
                            if (constant != null)
                                switchModel = new UniDirectionalSwitch(constant, output1);
                            else
                                switchModel = new RealSwitch(in1, in2);
                        }
                    } else
                        switchModel = new UniDirectionalSwitch(input1, output2);
                } else {
                    if (input2 instanceof CommonBusValue) {
                        switchModel = new UniDirectionalSwitch(input2, output1);
                    } else {
                        throw new NodeException(Lang.get("err_switchHasNoNet"), output1, output2);
                    }
                }
                break;
            case FROM1TO2:
                switchModel = new UniDirectionalSwitch(input1, output2);
                break;
            case FROM2TO1:
                switchModel = new UniDirectionalSwitch(input2, output1);
                break;
        }
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(output1, output2);
    }

    @Override
    public void registerNodes(Model model) {
    }

    @Override
    public void init(Model model) {
        switchModel.setModel(model);
        switchModel.setClosed(closed);
        hasChanged();
    }

    @Override
    public void hasChanged() {
        switchModel.propagate();
    }

    /**
     * Sets the closed state of the switch
     *
     * @param closed true if closed
     */
    public void setClosed(boolean closed) {
        if (this.closed != closed) {
            this.closed = closed;
            switchModel.setClosed(closed);
            hasChanged();
        }
    }

    /**
     * @return true if switch is closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @return output 1
     */
    ObservableValue getOutput1() {
        return output1;
    }

    /**
     * @return output 2
     */
    ObservableValue getOutput2() {
        return output2;
    }

    interface SwitchModel {
        void propagate();

        void setClosed(boolean closed);

        void setModel(Model model);
    }

    /**
     * A simple unidirectional switch.
     * Works like a driver: When the switch is closed, the signal value at the input
     * is forwarded to the output. When the switch is open, the output is set to HighZ.
     */
    private static final class UniDirectionalSwitch implements SwitchModel {
        private final ObservableValue input;
        private final ObservableValue output;
        private boolean closed;

        UniDirectionalSwitch(ObservableValue input, ObservableValue output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public void propagate() {
            if (closed) {
                output.set(input.getValue(), input.getHighZ());
            } else {
                output.setToHighZ();
            }
        }

        @Override
        public void setClosed(boolean closed) {
            this.closed = closed;
        }

        @Override
        public void setModel(Model model) {
        }
    }

    /**
     * Represents a real bidirectional switch.
     */
    public static final class RealSwitch implements SwitchModel {
        private final CommonBusValue input1;
        private final CommonBusValue input2;
        private BusModelStateObserver obs;

        private RealSwitch(CommonBusValue input1, CommonBusValue input2) {
            this.input1 = input1;
            this.input2 = input2;
        }

        @Override
        public void propagate() {
        }

        @Override
        public void setClosed(boolean closed) {
            obs.setClosed(this, closed);
        }

        @Override
        public void setModel(Model model) {
            obs = model.getObserver(BusModelStateObserver.class);
        }

        /**
         * @return the left hand side net
         */
        public CommonBusValue getInput1() {
            return input1;
        }

        /**
         * @return the right hand side net
         */
        public CommonBusValue getInput2() {
            return input2;
        }
    }
}
