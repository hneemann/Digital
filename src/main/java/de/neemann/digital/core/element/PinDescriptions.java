/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.element;

import de.neemann.digital.core.ObservableValues;

/**
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

    /**
     * Sets this key to the PinInfo instances of this list
     *
     * @param key the key to set
     * @return this for chained calls
     */
    public PinDescriptions setLangKey(String key) {
        for (PinDescription pd : this) {
            if (pd instanceof PinInfo) {
                ((PinInfo) pd).setLangKey(key);
            } else {
                System.out.println("PinDescription id not a PinInfo but " + pd.getClass().getSimpleName());
            }
        }
        return this;
    }
}
