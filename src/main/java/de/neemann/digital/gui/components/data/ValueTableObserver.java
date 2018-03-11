/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserverTyped;
import de.neemann.digital.core.Signal;
import de.neemann.digital.data.Value;
import de.neemann.digital.data.ValueTable;

import java.util.ArrayList;

/**
 * Observer to create measurement data
 */
public class ValueTableObserver implements ModelStateObserverTyped {

    private final ValueTable logData;
    private final ModelEvent type;
    private final ArrayList<Signal> signals;

    private Value[] manualSample;

    /**
     * Creates a new instance
     *
     * @param microStep true if gate base logging required
     * @param signals   the signals to log
     * @param maxSize   the maximum number of data points to store
     */
    public ValueTableObserver(boolean microStep, ArrayList<Signal> signals, int maxSize) {
        this.signals = signals;
        if (microStep)
            this.type = ModelEvent.MICROSTEP;
        else
            this.type = ModelEvent.STEP;

        String[] names = new String[signals.size()];
        for (int i = 0; i < signals.size(); i++)
            names[i] = signals.get(i).getName();
        this.logData = new ValueTable(names).setMaxSize(maxSize);
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (event == ModelEvent.STARTED)
            logData.clear();

        if (event == ModelEvent.MANUALCHANGE && type == ModelEvent.MICROSTEP) {
            if (manualSample == null)
                manualSample = new Value[logData.getColumns()];
            for (int i = 0; i < logData.getColumns(); i++)
                manualSample[i] = new Value(signals.get(i).getValue());
        }

        if (event == type) {
            if (manualSample != null) {
                logData.add(manualSample);
                manualSample = null;
            }
            Value[] row = new Value[logData.getColumns()];
            for (int i = 0; i < logData.getColumns(); i++)
                row[i] = new Value(signals.get(i).getValue());
            logData.add(row);
        }
    }

    @Override
    public ModelEvent[] getEvents() {
        return new ModelEvent[]{type, ModelEvent.STARTED, ModelEvent.MANUALCHANGE};
    }

    /**
     * @return the value table
     */
    public ValueTable getLogData() {
        return logData;
    }
}
