package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Model;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class DataSample {

    private final int mainTime;
    private final long[] values;

    public DataSample(int mainTime, int valueCount) {
        this.mainTime = mainTime;
        values = new long[valueCount];
    }

    public DataSample(DataSample sample) {
        this(sample.mainTime, sample.values.length);
        System.arraycopy(sample.values, 0, values, 0, values.length);
    }

    public int getMainTime() {
        return mainTime;
    }

    public long getValue(int i) {
        return values[i];
    }

    public void setValue(int i, long value) {
        values[i] = value;
    }

    public DataSample fillWith(ArrayList<Model.Signal> signals) {
        for (int i = 0; i < signals.size(); i++)
            values[i] = signals.get(i).getValue().getValueIgnoreBurn();
        return this;
    }
}
