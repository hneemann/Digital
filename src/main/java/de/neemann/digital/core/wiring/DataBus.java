package de.neemann.digital.core.wiring;

import de.neemann.digital.core.BurnException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.draw.elements.Pin;
import de.neemann.digital.gui.draw.elements.PinException;
import de.neemann.digital.gui.draw.model.Net;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class DataBus {
    private final CommonObservableValue commonOut;

    public DataBus(Net net, ArrayList<Pin> outputs) throws PinException {
        this(net, createArray(outputs));
    }

    private static ObservableValue[] createArray(ArrayList<Pin> outputs) {
        ObservableValue[] o = new ObservableValue[outputs.size()];
        for (int i = 0; i < outputs.size(); i++)
            o[i] = outputs.get(i).getValue();
        return o;
    }

    public DataBus(Net net, ObservableValue... outputs) throws PinException {
        int bits = 0;
        for (ObservableValue o : outputs) {
            int b = o.getBits();
            if (bits == 0) bits = b;
            else {
                if (bits != b)
                    throw new PinException(Lang.get("err_notAllOutputsSameBits"), net);
            }
            if (!o.supportsHighZ())
                throw new PinException(Lang.get("err_notAllOutputsSupportHighZ"), net);
        }
        commonOut = new CommonObservableValue(bits);

        CommonObserver observer = new CommonObserver(commonOut, outputs);
        for (ObservableValue p : outputs)
            p.addObserver(observer);
    }

    public ObservableValue getReadeableOutput() {
        return commonOut;
    }

    private static class CommonObserver implements Observer {

        private final CommonObservableValue commonOut;
        private final ObservableValue[] inputs;
        private boolean burn;

        CommonObserver(CommonObservableValue commonOut, ObservableValue[] outputs) {
            this.commonOut = commonOut;
            inputs = outputs;
        }

        @Override
        public void hasChanged() {
            long value = 0;
            burn = false;
            boolean highz = true;
            for (int i = 0; i < inputs.length; i++) {
                if (!inputs[i].isHighZ()) {
                    if (!highz)
                        burn = true;
                    highz = false;
                    value = inputs[i].getValue();
                }
            }
            commonOut.set(value, highz, burn);
        }
    }


    private static class CommonObservableValue extends ObservableValue {
        private boolean burn;

        CommonObservableValue(int bits) {
            super("commmon", bits);
        }

        private void check() {
            if (burn) {
                throw new BurnException();
            }
        }

        @Override
        public long getValue() {
            check();
            return super.getValue();
        }

        @Override
        public boolean isHighZ() {
            check();
            return super.isHighZ();
        }

        public void set(long value, boolean highz, boolean burn) {
            super.set(value, highz);
            this.burn = burn;
        }
    }
}
