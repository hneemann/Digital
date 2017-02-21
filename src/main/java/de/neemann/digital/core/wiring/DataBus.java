package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.model.Net;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.HashSet;

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
    private final CommonBusValue observer;

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
        PinDescription.PullResistor resistor = PinDescription.PullResistor.none;
        for (ObservableValue o : outputs) {
            int b = o.getBits();
            if (bits == 0) bits = b;
            else {
                if (bits != b)
                    throw new PinException(Lang.get("err_notAllOutputsSameBits"), net);
            }
            if (!o.supportsHighZ())
                throw new PinException(Lang.get("err_notAllOutputsSupportHighZ"), net);

            switch (o.getPullResistor()) {
                case pullDown:
                    if (resistor == PinDescription.PullResistor.pullUp)
                        throw new PinException(Lang.get("err_pullUpAndDownNotAllowed"), net);
                    resistor = PinDescription.PullResistor.pullDown;
                    break;
                case pullUp:
                    if (resistor == PinDescription.PullResistor.pullDown)
                        throw new PinException(Lang.get("err_pullUpAndDownNotAllowed"), net);
                    resistor = PinDescription.PullResistor.pullUp;
                    break;
            }
        }

        BusModelStateObserver obs = model.getObserver(BusModelStateObserver.class);
        if (obs == null) {
            obs = new BusModelStateObserver();
            model.addObserver(obs);
        }

        observer = new CommonBusValue(bits, obs, outputs, resistor);
        for (ObservableValue p : outputs)
            p.addObserverToValue(observer);
        observer.hasChanged();
    }

    /**
     * Returns the readable ObservableValue for this bus
     *
     * @return the readable ObservableValue
     */
    public ObservableValue getReadableOutput() {
        return observer;
    }

    /**
     * This observer is added to all outputs connected together
     */
    public static final class CommonBusValue extends ObservableValue implements Observer {
        private final BusModelStateObserver obs;
        private final ObservableValue[] inputs;
        private final PinDescription.PullResistor resistor;
        private boolean burn;
        private int addedVersion = -1;

        private CommonBusValue(int bits, BusModelStateObserver obs, ObservableValue[] outputs, PinDescription.PullResistor resistor) {
            super("commonBusOut", bits, resistor.equals(PullResistor.none));
            this.obs = obs;
            inputs = outputs;
            this.resistor = resistor;
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
            if (highz) {
                switch (resistor) {
                    case pullUp:
                        set(-1, false);
                        break;
                    case pullDown:
                        set(0, false);
                        break;
                    default:
                        set(value, highz);
                }
            } else
                set(value, highz);

            // if burn condition and not yet added for post step check add for post step check
            if (burn && (obs.version != addedVersion)) {
                addedVersion = obs.version;
                obs.addCheck(this);
            }

        }

        private void checkBurn() {
            if (burn)
                throw new BurnException(this);
        }
    }

    /**
     * Checks if a temporary burn condition is still present after the step is completed.
     * If so an exception is thrown.
     * Handles also the switches
     */
    public static final class BusModelStateObserver implements ModelStateObserver {
        private final ArrayList<CommonBusValue> busList;
        private int version;

        private BusModelStateObserver() {
            busList = new ArrayList<>();
        }

        @Override
        public void handleEvent(ModelEvent event) {
            if (event == ModelEvent.STEP && !busList.isEmpty()) {
                for (CommonBusValue bus : busList) {
                    bus.checkBurn();
                }
                busList.clear();
                version++;
            }
        }

        private void addCheck(CommonBusValue commonBusValue) {
            busList.add(commonBusValue);
        }

        public void setClosed(Switch.RealSwitch realSwitch, boolean closed) {

        }
    }

}
