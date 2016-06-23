package de.neemann.digital.gui;

import java.io.File;

/**
 * Created by helmut.neemann on 23.06.2016.
 */
public interface DigitalRemoteInterface {

    /**
     * Loads the given file to the data rom
     *
     * @param file the file to load
     */
    void loadRom(File file);

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

}
