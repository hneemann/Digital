package de.neemann.digital.gui;

import java.io.File;

/**
 * Interface which is used by the remote server
 * <p>
 * Created by helmut.neemann on 23.06.2016.
 */
public interface DigitalRemoteInterface {

    /**
     * Loads the given file to the data rom
     *
     * @param file the file to load
     * @return true if loading was successful
     */
    boolean loadRom(File file);

    /**
     * Starts the model
     */
    void start();

    /**
     * performs a single step
     */
    void doSingleStep();

    /**
     * runs model to the next BRK instruction
     */
    void runToBreak();

    /**
     * stops the model
     */
    void stop();
}
