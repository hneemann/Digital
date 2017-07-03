package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.core.Signal;
import de.neemann.digital.data.Value;
import de.neemann.digital.data.ValueTable;

import java.util.ArrayList;

/**
 * Observer to create measurement data
 *
 * @author hneemann
 */
public class DataSetObserver implements ModelStateObserver {

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
    public DataSetObserver(boolean microStep, ArrayList<Signal> signals, int maxSize) {
        this.signals = signals;
        if (microStep)
            this.type = ModelEvent.MICROSTEP;
        else
            this.type = ModelEvent.STEP;

        this.logData = new ValueTable(createNames(signals)).setMaxSize(maxSize);
    }

    private String[] createNames(ArrayList<Signal> signals) {
        String[] names = new String[signals.size()];
        for (int i = 0; i < signals.size(); i++)
            names[i] = signals.get(i).getName();
        return names;
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (event == ModelEvent.STARTED) {
            logData.clear();
        }
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

    /**
     * @return the value table
     */
    public ValueTable getLogData() {
        return logData;
    }
}
