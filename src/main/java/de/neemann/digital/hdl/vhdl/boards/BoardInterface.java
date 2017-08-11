package de.neemann.digital.hdl.vhdl.boards;

import de.neemann.digital.hdl.model.HDLModel;

import java.io.File;
import java.io.IOException;

/**
 * Interface to write the additional files
 */
public interface BoardInterface {
    /**
     * Writed additional files
     *
     * @param path  the target path
     * @param model the model
     * @throws IOException IOException
     */
    void writeFiles(File path, HDLModel model) throws IOException;

    /**
     * @return clock period in ns
     */
    int getClockPeriod();

}
