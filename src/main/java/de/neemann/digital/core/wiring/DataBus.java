package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.model.Net;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * Handles the creation of a data bus.
 * Is needed to connect multiple outputs which can become high Z.
 * If one of the output becomes low Z, this value is returned by the {@link ObservableValue}
 * created by this bus. If more then one output becomes low Z and the values are not equal then
 * a {@link BurnException} is thrown after the models step is completed.
 * During the calculation of of a single step a temporary burn condition is allowed.
 *
 * @author hneemann
 */
public class DataBus {
    private final ObservableValue commonOut;

    /**
     * Creates a new data bus
     *
     * @param net     the net
     * @param model   the model
     * @param outputs the outputs building the net
     * @throws PinException PinException
     */
    public DataBus(Net net, Model model, ArrayList<Pin> outputs) throws PinException {
        this(net, model, createArray(outputs));
    }

    private static ObservableValue[] createArray(ArrayList<Pin> outputs) {
        ObservableValue[] o = new ObservableValue[outputs.size()];
        for (int i = 0; i < outputs.size(); i++)
            o[i] = outputs.get(i).getValue();
        return o;
    }

    /**
     * Creates a new data bus
     *
     * @param net     the net
     * @param model   the model
     * @param outputs the outputs building the net
     * @throws PinException PinException
     */
    public DataBus(Net net, Model model, ObservableValue... outputs) throws PinException {
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
        commonOut = new ObservableValue("common", bits);

        BusModelStateObserver obs = model.getObserver(BusModelStateObserver.class);
        if (obs == null) {
            obs = new BusModelStateObserver();
            model.addObserver(obs);
        }

        CommonBusObserver observer = new CommonBusObserver(commonOut, obs, outputs);
        for (ObservableValue p : outputs)
            p.addObserver(observer);
        observer.hasChanged();
    }

    /**
     * Returns the readable ObservableValue for this bus
     *
     * @return the readable ObservableValue
     */
    public ObservableValue getReadableOutput() {
        return commonOut;
    }

    private static class CommonBusObserver implements Observer {

        private final ObservableValue commonOut;
        private final BusModelStateObserver obs;
        private final ObservableValue[] inputs;
        private boolean burn;
        private int addedVersion = -1;

        CommonBusObserver(ObservableValue commonOut, BusModelStateObserver obs, ObservableValue[] outputs) {
            this.commonOut = commonOut;
            this.obs = obs;
            inputs = outputs;
        }

        @Override
        public void hasChanged() {
            long value = 0;
            burn = false;
            boolean highz = true;
            for (int i = 0; i < inputs.length; i++) {
                if (!inputs[i].isHighZ()) {
                    if (highz) {
                        highz = false;
                        value = inputs[i].getValue();
                    } else {
                        if (value != inputs[i].getValue())
                            burn = true;
                    }
                }
            }
            commonOut.set(value, highz);

            // if burn condition and not yet added for post step check add for post step check
            if (burn && (obs.version != addedVersion)) {
                addedVersion = obs.version;
                obs.addCheck(this);
            }

        }

        private void checkBurn() {
            if (burn)
                throw new BurnException(commonOut);
        }
    }

    private static final class BusModelStateObserver implements ModelStateObserver {
        private final ArrayList<CommonBusObserver> busList;
        private int version;

        private BusModelStateObserver() {
            busList = new ArrayList<>();
        }

        @Override
        public void handleEvent(ModelEvent event) {
            if (event == ModelEvent.STEP && !busList.isEmpty()) {
                for (CommonBusObserver bus : busList) {
                    bus.checkBurn();
                }
                busList.clear();
                version++;
            }
        }

        private void addCheck(CommonBusObserver commonBusObserver) {
            busList.add(commonBusObserver);
        }
    }

}
