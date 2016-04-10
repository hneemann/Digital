package de.neemann.digital.draw.graphics;

import java.io.File;
import java.io.IOException;

/**
 * Factor to create a {@link Graphic} instance suited to create a file.
 *
 * @author hneemann
 */
public interface Exporter {
    /**
     * Creates a {@link Graphic} instance
     *
     * @param file the filename to use
     * @param min  upper right corner of the circuit
     * @param max  lower left corner of the circuit
     * @return the {@link Graphic} instance to use
     * @throws IOException IOException
     */
    Graphic create(File file, Vector min, Vector max) throws IOException;
}
