package de.neemann.digital.gui.components.progress;

/**
 * ProgressConsole prints the progess to stdout.
 * Created by hneemann on 14.03.17.
 */
public class ProgressConsole implements ProgressListener {

    private final String name;
    private int max;
    private int pos;
    private int lastOut;

    /**
     * Creates a new instance
     */
    public ProgressConsole() {
        name = null;
    }

    /**
     * Creates a new instance
     *
     * @param max  the max progress value
     * @param name the name of this listener. Is printed in front of each line
     */
    public ProgressConsole(int max, String name) {
        this.max = max;
        this.name = name;
    }


    /**
     * Creates a new instance
     *
     * @param max the max progress value
     */
    public ProgressConsole(int max) {
        this.max = max;
        name = null;
    }

    @Override
    public void setStart(int max) {
        this.max = max;
        pos = 0;
    }

    @Override
    public void inc() {
        pos++;

        int out = (pos * 100) / max;
        if (out != lastOut) {
            lastOut = out;
            if (name == null)
                System.out.println("\r" + lastOut + "%");
            else
                System.out.println("\r" + name + " " + lastOut + "%");
        }
    }

    @Override
    public void finish() {
        System.out.println();
    }
}
