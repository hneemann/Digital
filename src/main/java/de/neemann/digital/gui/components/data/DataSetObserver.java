package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;

/**
 * Observer to create measurement data
 *
 * @author hneemann
 */
public class DataSetObserver implements ModelStateObserver {

    private final DataSet dataSet;
    private final ModelEvent type;

    private DataSample manualSample;
    private int maintime;

    /**
     * Creates a new instance
     *
     * @param microStep true if gate base logging required
     * @param dataSet   the dataset to fill
     */
    public DataSetObserver(boolean microStep, DataSet dataSet) {
        if (microStep)
            this.type = ModelEvent.MICROSTEP;
        else
            this.type = ModelEvent.STEP;

        this.dataSet = dataSet;
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (event == ModelEvent.STARTED) {
            dataSet.clear();
            maintime = 0;
        }
        if (event == ModelEvent.MANUALCHANGE && type == ModelEvent.MICROSTEP) {
            if (manualSample == null)
                manualSample = new DataSample(maintime, dataSet.signalSize());
            manualSample.fillWith(dataSet.getSignals());
        }

        if (event == type) {
            if (manualSample != null) {
                dataSet.add(manualSample);
                manualSample = null;
                maintime++;
            }
            dataSet.add(new DataSample(maintime, dataSet.signalSize()).fillWith(dataSet.getSignals()));
            maintime++;
        }
    }

}
