package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Signal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A DataSample contains all the values of the signals to collect data for.
 * Only the data of a single timestamp is stored in one sample.
 *
 * @author hneemann
 */
public class DataSample {

    private final int timeStamp;
    private final long[] values;

    /**
     * Creates a new sample
     *
     * @param timeStamp  the time stamp
     * @param valueCount the number of values, all values are set to zero
     */
    public DataSample(int timeStamp, int valueCount) {
        this.timeStamp = timeStamp;
        values = new long[valueCount];
    }

    /**
     * @param sample a deep copy of the given sample
     */
    public DataSample(DataSample sample) {
        this(sample.timeStamp, sample.values.length);
        System.arraycopy(sample.values, 0, values, 0, values.length);
    }

    /**
     * @return returns the timestamp
     */
    public int getTimeStamp() {
        return timeStamp;
    }

    /**
     * returns a value
     *
     * @param i indes of the value
     * @return the value
     */
    public long getValue(int i) {
        return values[i];
    }

    /**
     * sets a value in  the sample
     *
     * @param i     the index of the value
     * @param value the value
     * @return this for chained calls
     */
    public DataSample setValue(int i, long value) {
        values[i] = value;
        return this;
    }

    /**
     * Fills this sample with the actual signals values
     *
     * @param signals the signals to create a sample from
     * @return the sample to allow chained calls
     */
    public DataSample fillWith(ArrayList<Signal> signals) {
        for (int i = 0; i < signals.size(); i++)
            values[i] = signals.get(i).getValue().getValueIgnoreBurn();
        return this;
    }

    /**
     * Write this sample as a single CSV line
     *
     * @param w the writer
     * @throws IOException IOException
     */
    public void writeTo(BufferedWriter w) throws IOException {
        w.write("\"" + timeStamp + "\"");
        for (int i = 0; i < values.length; i++)
            w.write(",\"" + values[i] + "\"");
    }
}
