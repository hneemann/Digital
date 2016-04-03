package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Model;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The dataSet stores the collected DataSamples.
 * Every DataSample contains the values of al signals at a given time.
 *
 * @author hneemann
 */
public class DataSet implements Iterable<DataSample> {
    private static final int MAX_SAMPLES = 1000;
    private final ArrayList<Model.Signal> signals;
    private final ArrayList<DataSample> samples;
    private DataSample min;
    private DataSample max;

    /**
     * Creates a new instance
     *
     * @param signals the signals used to collect DataSamples
     */
    public DataSet(ArrayList<Model.Signal> signals) {
        this.signals = signals;
        samples = new ArrayList<>();
    }

    /**
     * Adds a new Datasample
     *
     * @param sample the DataSample
     */
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

    /**
     * @return the mumber of samples
     */
    public int size() {
        return samples.size();
    }

    /**
     * @return the number of signals
     */
    public int signalSize() {
        return signals.size();
    }

    @Override
    public Iterator<DataSample> iterator() {
        return samples.iterator();
    }

    /**
     * @return a sample which contains all the minimum values
     */
    public DataSample getMin() {
        return min;
    }

    /**
     * @return a sample which contains all the maximum values
     */
    public DataSample getMax() {
        return max;
    }

    /**
     * Gets the width of the signal with the given index
     *
     * @param i the index of the signal
     * @return max-min
     */
    public long getWidth(int i) {
        return max.getValue(i) - min.getValue(i);
    }

    /**
     * return the signal with the given index
     *
     * @param i the index
     * @return the signal
     */
    public Model.Signal getSignal(int i) {
        return signals.get(i);
    }
}
