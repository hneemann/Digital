package de.neemann.digital.hdl.vhdl.boards;

import java.io.File;
import java.io.IOException;

/**
 * Interface to write the additional files
 */
public interface BoardInterface {
    /**
     * Writed additional files
     *
     * @param path the target path
     * @throws IOException IOException
     */
    void writeFiles(File path) throws IOException;
}
