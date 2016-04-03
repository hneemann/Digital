package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Model;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author hneemann
 */
public class DataSet implements Iterable<DataSample> {
    private static final int MAX_SAMPLES = 1000;
    private final ArrayList<Model.Signal> signals;
    private final ArrayList<DataSample> samples;
    private DataSample min;
    private DataSample max;

    public DataSet(ArrayList<Model.Signal> signals) {
        this.signals = signals;
        samples = new ArrayList<>();
    }

    public void add(DataSample sample) {
        if (samples.size() < MAX_SAMPLES) {
            samples.add(sample);
            if (min == null) {
                min = new DataSample(sample);
                max = new DataSample(sample);
            } else {
                for (int i = 0; i < signals.size(); i++) {
                    if (sample.getValue(i) < min.getValue(i))
                        min.setValue(i, sample.getValue(i));
                    if (sample.getValue(i) > max.getValue(i))
                        max.setValue(i, sample.getValue(i));
                }
            }
        }
    }

    public int size() {
        return samples.size();
    }

    public int signalSize() {
        return signals.size();
    }

    @Override
    public Iterator<DataSample> iterator() {
        return samples.iterator();
    }

    public DataSample getMin() {
        return min;
    }

    public DataSample getMax() {
        return max;
    }

    public long getWidth(int i) {
        return max.getValue(i) - min.getValue(i);
    }

    public Model.Signal getSignal(int i) {
        return signals.get(i);
    }
}
