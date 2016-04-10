package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;

/**
 * @author hneemann
 */
public class DataSetObserver implements ModelStateObserver {

    private final DataSet dataSet;
    private final ModelEvent type;

    private DataSample manualSample;
    private int maintime;

    public DataSetObserver(ModelEvent type, DataSet dataSet) {
        this.type = type;
        this.dataSet = dataSet;
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (event == ModelEvent.STARTED) {
            dataSet.clear();
            maintime = 0;
        }
        if (event == ModelEvent.MANUALCHANGE) {
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
