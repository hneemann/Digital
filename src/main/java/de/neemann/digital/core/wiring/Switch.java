package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A simple switch
 */
public class Switch implements Element, Observer {

    /**
     * The diodes description
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
        bits = attr.getBits();
        closed = attr.get(Keys.CLOSED);
        output1 = new ObservableValue("out1", bits, true).setPinDescription(DESCRIPTION);
        output2 = new ObservableValue("out2", bits, true).setPinDescription(DESCRIPTION);
        output1.set(0, true);
        output2.set(0, true);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        ObservableValue input1 = inputs.get(0).addObserverToValue(this).checkBits(bits, null);
        ObservableValue input2 = inputs.get(1).addObserverToValue(this).checkBits(bits, null);
        if (input1 instanceof DataBus.CommonBusValue) {
            if (input2 instanceof DataBus.CommonBusValue) {
                switchModel = new RealSwitch((DataBus.CommonBusValue) input1, (DataBus.CommonBusValue) input2);
            } else {
                switchModel = new SimpleSwitch(input1, output2);
            }
        } else {
            if (input2 instanceof DataBus.CommonBusValue) {
                switchModel = new SimpleSwitch(input2, output1);
            } else {
                throw new NodeException("err_switchHasNoNet", output1, output2);
            }
        }
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(output1, output2);
    }

    @Override
    public void registerNodes(Model model) {
        switchModel.setModel(model);
        switchModel.setClosed(closed);
    }

    @Override
    public void hasChanged() {
        switchModel.propagate();
    }

    @Override
    public void init(Model model) throws NodeException {
        hasChanged();
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

    public static class RealSwitch implements SwitchModel {
        private final DataBus.CommonBusValue input1;
        private final DataBus.CommonBusValue input2;
        private DataBus.BusModelStateObserver obs;

        private RealSwitch(DataBus.CommonBusValue input1, DataBus.CommonBusValue input2) {
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
            obs = model.getObserver(DataBus.BusModelStateObserver.class);
        }

        public DataBus.CommonBusValue getInput1() {
            return input1;
        }

        public DataBus.CommonBusValue getInput2() {
            return input2;
        }
    }
}
