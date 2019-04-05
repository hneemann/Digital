/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Instance used by the model to share signal values.
 * Used by the FSM dialog to indicate the current state.
 * Up to now only probe values are shared.
 */
public final class GlobalValues {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalValues.class);
    private static GlobalValues ourInstance = new GlobalValues();

    /**
     * @return returns the global instance
     */
    public static GlobalValues getInstance() {
        return ourInstance;
    }

    private ArrayList<GlobalValueListener> listeners = new ArrayList<>();

    private GlobalValues() {
    }

    /**
     * Registers a value
     *
     * @param name  the name of the value
     * @param value the value itself
     * @param model the model the value belongs to
     */
    public void register(String name, ObservableValue value, Model model) {
        for (GlobalValueListener l : listeners)
            l.valueCreated(name, value, model);
    }

    /**
     * Adds a listener for global values
     *
     * @param listener the listener to add
     */
    public void addListener(GlobalValueListener listener) {
        listeners.add(listener);
        LOGGER.debug("global value listener added " + listeners.size());
    }

    /**
     * Removes a listener for global values
     *
     * @param listener the listener to remove
     */
    public void removeListener(GlobalValueListener listener) {
        listeners.remove(listener);
        LOGGER.debug("global value listener removed " + listeners.size());
    }

    /**
     * A listener for global values
     */
    public interface GlobalValueListener {
        /**
         * Called if a value is created
         *
         * @param name  the name of the value
         * @param value the value itself
         * @param model the model the value belongs to
         */
        void valueCreated(String name, ObservableValue value, Model model);
    }
}
