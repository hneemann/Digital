package de.neemann.digital.core.element;

import de.neemann.digital.core.ObservableValues;

/**
 * @author hneemann
 */
public class PinDescriptions extends ImmutableList<PinDescription> {
    /**
     * Creates a new Instance
     *
     * @param items the items to store
     */
    public PinDescriptions(PinDescription... items) {
        super(items);
    }

    /**
     * Creates a new Instance
     *
     * @param observableValues the items to store
     */
    public PinDescriptions(ObservableValues observableValues) {
        super(observableValues);
    }

    public PinDescriptions setLangKey(String key) {
        for (PinDescription pd : this) {
            if (pd instanceof PinInfo) {
                ((PinInfo)pd).setLangKey(key);
            } else {
                System.out.println("no PinInfo: "+pd.getClass().getSimpleName());
            }
        }
        return this;
    }
}
