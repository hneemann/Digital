package de.neemann.digital.core.wiring;

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
public class Switch implements Element, Observer {

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

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public Switch(ElementAttributes attr) {
        this(attr, attr.get(Keys.CLOSED));
    }

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public Switch(ElementAttributes attr, boolean closed) {
        bits = attr.getBits();
        this.closed = closed;
        output1 = new ObservableValue("out1", bits, true).setPinDescription(DESCRIPTION).setBidirectional(true);
        output2 = new ObservableValue("out2", bits, true).setPinDescription(DESCRIPTION).setBidirectional(true);
        output1.set(0, true);
        output2.set(0, true);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        ObservableValue input1 = inputs.get(0).addObserverToValue(this).checkBits(bits, null);
        ObservableValue input2 = inputs.get(1).addObserverToValue(this).checkBits(bits, null);
        if (input1 instanceof CommonBusValue) {
            if (input2 instanceof CommonBusValue) {
                switchModel = new RealSwitch((CommonBusValue) input1, (CommonBusValue) input2);
            } else {
                switchModel = new SimpleSwitch(input1, output2);
            }
        } else {
            if (input2 instanceof CommonBusValue) {
                switchModel = new SimpleSwitch(input2, output1);
            } else {
                throw new NodeException(Lang.get("err_switchHasNoNet"), output1, output2);
            }
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
    public void init(Model model) throws NodeException {
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
     * @return output 1
     */
    protected ObservableValue getOutput1() {
        return output1;
    }

    /**
     * @return output 2
     */
    protected ObservableValue getOutput2() {
        return output2;
    }

    interface SwitchModel {
        void propagate();

        void setClosed(boolean closed);

        void setModel(Model model);
    }

    private static final class SimpleSwitch implements SwitchModel {
        private final ObservableValue input;
        private final ObservableValue output;
        private boolean closed;

        SimpleSwitch(ObservableValue input, ObservableValue output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public void propagate() {
            if (closed) {
                output.set(input.getValue(), input.isHighZ());
            } else {
                output.set(0, true);
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
     * represents a switch
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
