package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Signal;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.shapes.Drawable;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The dataSet stores the collected DataSamples.
 * Every DataSample contains the values of al signals at a given time.
 *
 * @author hneemann
 */
public class DataSet implements Iterable<DataSample>, Drawable {
    private final ArrayList<Signal> signals;
    private final int maxSize;
    private final ArrayList<DataSample> samples;
    private final int maxTextLength;
    private DataSample min;
    private DataSample max;

    /**
     * Creates a simple dummy DataSet used for creating the DataShape
     */
    public DataSet() {
        this(createDummy(), 20);
        add(new DataSample(0, signalSize()));
        add(new DataSample(1, signalSize()).setValue(1, 1));
    }

    private static ArrayList<Signal> createDummy() {
        ArrayList<Signal> list = new ArrayList<>();
        list.add(new Signal("A", null));
        list.add(new Signal("B", null));
        list.add(new Signal("C", null));
        return list;
    }

    /**
     * Creates a new instance
     *
     * @param signals the signals used to collect DataSamples
     * @param maxSize max size of data elements
     */
    public DataSet(ArrayList<Signal> signals, int maxSize) {
        this.signals = signals;
        this.maxSize = maxSize;
        samples = new ArrayList<>();
        int tl = 0;
        for (int i = 0; i < signalSize(); i++) {
            String text = getSignal(i).getName();
            int w = text.length();
            if (w > tl) tl = w;
        }
        maxTextLength = tl;
    }

    /**
     * Deletes all data
     */
    public void clear() {
        samples.clear();
        min = null;
        max = null;
    }

    /**
     * Adds a new Datasample
     *
     * @param sample the DataSample
     */
    public void add(DataSample sample) {
        while (samples.size() >= maxSize)
            samples.remove(0);

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
    public Signal getSignal(int i) {
        return signals.get(i);
    }


    private static final int BORDER = 10;
    private static final int SIZE = 25;
    private static final int SEP2 = 5;
    private static final int SEP = SEP2 * 2;


    @Override
    public void drawTo(Graphic g, boolean highLight) {
        int x = getTextBorder();

        int yOffs = SIZE / 2;
        int y = BORDER;
        for (int i = 0; i < signalSize(); i++) {
            String text = getSignal(i).getName();
            g.drawText(new Vector(x - 2, y + yOffs), new Vector(x + 1, y + yOffs), text, Orientation.RIGHTCENTER, Style.NORMAL);
            g.drawLine(new Vector(x, y - SEP2), new Vector(x + SIZE * size(), y - SEP2), Style.DASH);
            y += SIZE + SEP;
        }
        g.drawLine(new Vector(x, y - SEP2), new Vector(x + SIZE * size(), y - SEP2), Style.DASH);


        int[] lastRy = new int[signalSize()];
        boolean first = true;
        for (DataSample s : this) {
            g.drawLine(new Vector(x, BORDER - SEP2), new Vector(x, (SIZE + SEP) * signalSize() + BORDER - SEP2), Style.DASH);
            y = BORDER;
            for (int i = 0; i < signalSize(); i++) {

                long width = getWidth(i);
                if (width == 0) width = 1;
                //int ry = (int) (SIZE-(SIZE*(s.getValue(i)-dataSet.getMin().getValue(i)))/ width);
                int ry = (int) (SIZE - (SIZE * s.getValue(i)) / width);
                g.drawLine(new Vector(x, y + ry), new Vector(x + SIZE, y + ry), Style.NORMAL);
                if (!first && ry != lastRy[i])
                    g.drawLine(new Vector(x, y + lastRy[i]), new Vector(x, y + ry), Style.NORMAL);

                lastRy[i] = ry;
                y += SIZE + SEP;
            }
            first = false;
            x += SIZE;
        }
        g.drawLine(new Vector(x, BORDER - SEP2), new Vector(x, (SIZE + SEP) * signalSize() + BORDER - SEP2), Style.DASH);
    }

    private int getTextBorder() {
        return maxTextLength * Style.NORMAL.getFontSize() / 2 + BORDER + SEP;
    }

    /**
     * @return the preferred width of the graphical representation
     */
    public int getGraphicWidth() {
        return getTextBorder() + size() * SIZE;
    }

    /**
     * @return the preferred height of the graphical representation
     */
    public int getGraphicHeight() {
        return signalSize() * (SIZE + SEP) + 2 * BORDER;
    }

    /**
     * @return the signals stored
     */
    public ArrayList<Signal> getSignals() {
        return signals;
    }

    /**
     * Stores the data in  csv file
     *
     * @param file the file
     * @throws IOException IOException
     */
    public void saveCSV(File file) throws IOException {
        saveCSV(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
    }

    /**
     * Stores the data in  csv file
     *
     * @param w the writer
     * @throws IOException IOException
     */
    public void saveCSV(BufferedWriter w) throws IOException {
        try {
            w.write("\"step\"");
            for (Signal s : signals)
                w.write(",\"" + s.getName() + '"');
            w.write("\n");
            for (DataSample s : samples) {
                s.writeTo(w);
                w.write("\n");
            }
        } finally {
            w.close();
        }
    }
}
