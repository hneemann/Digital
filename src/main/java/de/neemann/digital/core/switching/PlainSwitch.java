/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.switching;

import de.neemann.digital.core.*;
import de.neemann.digital.core.wiring.bus.BusModelStateObserver;
import de.neemann.digital.core.wiring.bus.CommonBusValue;
import de.neemann.digital.lang.Lang;

/**
 * A simple switch
 */
public final class PlainSwitch implements NodeInterface {

    /**
     * Defines a direction for the switch. NO means no direction is given, the switch is bidirectional.
     */
    public enum Unidirectional {NO, FROM1TO2, FROM2TO1}

    private final ObservableValue output1;
    private final ObservableValue output2;
    private final int bits;
    private boolean closed;
    private SwitchModel switchModel;
    private Unidirectional unidirectional = Unidirectional.NO;

    /**
     * Creates a new instance
     *
     * @param bits   the number of bits
     * @param closed initial state
     * @param out1   name of output 1
     * @param out2   name of output 2
     */
    PlainSwitch(int bits, boolean closed, String out1, String out2) {
        this.bits = bits;
        this.closed = closed;
        output1 = new ObservableValue(out1, bits).setBidirectional().setToHighZ().setDescription(Lang.get("elem_Switch_pin")).setSwitchPin(true);
        output2 = new ObservableValue(out2, bits).setBidirectional().setToHighZ().setDescription(Lang.get("elem_Switch_pin")).setSwitchPin(true);
    }

    /**
     * Sets this switch to unidirectional
     *
     * @param unidirectional the state
     * @return this for chained calls
     */
    public PlainSwitch setUnidirectional(Unidirectional unidirectional) {
        this.unidirectional = unidirectional;
        return this;
    }

    /**
     * @return the number of data bits
     */
    public int getBits() {
        return bits;
    }

    /**
     * Sets the inputs of this switch
     *
     * @param input1 first input
     * @param input2 second input
     * @throws NodeException NodeException
     */
    public void setInputs(ObservableValue input1, ObservableValue input2) throws NodeException {
        // create a switch only if both sides are connected. null means pin is not connected
        if (input1 != null && input2 != null) {
            input1.addObserverToValue(this).checkBits(bits, null);
            input2.addObserverToValue(this).checkBits(bits, null);
            switch (unidirectional) {
                case NO:
                    switchModel = createSwitchModel(input1, input2, output1, output2);
                    break;
                case FROM1TO2:
                    switchModel = new UniDirectionalSwitch(input1, output2);
                    break;
                case FROM2TO1:
                    switchModel = new UniDirectionalSwitch(input2, output1);
                    break;
            }
        }
    }

    static SwitchModel createSwitchModel(
            ObservableValue input1, ObservableValue input2,
            ObservableValue output1, ObservableValue output2) throws NodeException {

        if (input1 instanceof CommonBusValue) {
            if (input2 instanceof CommonBusValue) {
                final CommonBusValue in1 = (CommonBusValue) input1;
                final CommonBusValue in2 = (CommonBusValue) input2;
                ObservableValue constant = in1.searchConstant();
                if (constant != null)
                    return new UniDirectionalSwitch(constant, output2);
                else {
                    constant = in2.searchConstant();
                    if (constant != null)
                        return new UniDirectionalSwitch(constant, output1);
                    else
                        return new RealSwitch(in1, output1, in2, output2);
                }
            } else
                return new UniDirectionalSwitch(input1, output2);
        } else {
            if (input2 instanceof CommonBusValue) {
                return new UniDirectionalSwitch(input2, output1);
            } else {
                throw new NodeException(Lang.get("err_switchHasNoNet"), output1, output2);
            }
        }
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(output1, output2);
    }

    /**
     * Adds the outputs to the given builder
     *
     * @param ov the builder to use
     */
    void addOutputsTo(ObservableValues.Builder ov) {
        ov.add(output1, output2);
    }

    /**
     * Initializes the switch
     *
     * @param model the model
     */
    public void init(Model model) {
        if (switchModel != null) {
            switchModel.setModel(model);
            switchModel.setClosed(closed);
            hasChanged();
        }
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
        if (switchModel != null) {
            if (this.closed != closed) {
                this.closed = closed;
                switchModel.setClosed(closed);
                hasChanged();
            }
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

        default void dontTouchOpenContact() {
        }

        default boolean isUniDirOutput(ObservableValue out) {
            return false;
        }
    }

    /**
     * A simple unidirectional switch.
     * Works like a driver: When the switch is closed, the signal value at the input
     * is forwarded to the output. When the switch is open, the output is set to HighZ.
     */
    private static final class UniDirectionalSwitch implements SwitchModel {
        private final ObservableValue input;
        private final ObservableValue output;
        private boolean setOpenContactToHighZ;
        private boolean closed;

        UniDirectionalSwitch(ObservableValue input, ObservableValue output) {
            this.input = input;
            this.output = output;
            this.setOpenContactToHighZ = true;
        }

        @Override
        public void propagate() {
            if (closed) {
                output.set(input.getValue(), input.getHighZ());
            } else {
                if (setOpenContactToHighZ)
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

        @Override
        public void dontTouchOpenContact() {
            setOpenContactToHighZ = false;
        }

        @Override
        public boolean isUniDirOutput(ObservableValue out) {
            return output == out;
        }
    }

    /**
     * Represents a real bidirectional switch.
     */
    public static final class RealSwitch implements SwitchModel {
        private final CommonBusValue input1;
        private final ObservableValue output1;
        private final CommonBusValue input2;
        private final ObservableValue output2;
        private BusModelStateObserver obs;

        private RealSwitch(CommonBusValue input1, ObservableValue output1, CommonBusValue input2, ObservableValue output2) {
            this.input1 = input1;
            this.output1 = output1;
            this.input2 = input2;
            this.output2 = output2;
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

        /**
         * @return the output used by the left hand side pin
         */
        public ObservableValue getOutput1() {
            return output1;
        }

        /**
         * @return the output used by the right hand side pin
         */
        public ObservableValue getOutput2() {
            return output2;
        }
    }
}
