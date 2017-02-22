package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.BurnException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.PinDescription;

import java.util.List;

/**
 * Created by hneemann on 22.02.17.
 */
public abstract class AbstractBusHandler {

    private final BusModelStateObserver obs;
    private boolean burn;
    private int addedVersion = -1;

    public AbstractBusHandler(BusModelStateObserver obs) {
        this.obs = obs;
    }

    public abstract List<ObservableValue> getInputs();

    public abstract PinDescription.PullResistor getResistor();

    public abstract void set(long value, boolean highz);

    public void recalculate() {
        long value = 0;
        burn = false;
        boolean highz = true;
        for (ObservableValue input : getInputs()) {
            if (!input.isHighZ()) {
                if (highz) {
                    highz = false;
                    value = input.getValue();
                } else {
                    if (value != input.getValue())
                        burn = true;
                }
            }
        }
        if (highz) {
            switch (getResistor()) {
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
        if (burn && (obs.getVersion() != addedVersion)) {
            addedVersion = obs.getVersion();
            obs.addCheck(this);
        }
    }

    public void checkBurn() {
        if (burn)
            throw new BurnException(getInputs());
    }


}
