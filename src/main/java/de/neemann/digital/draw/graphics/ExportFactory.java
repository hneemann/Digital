package de.neemann.digital.draw.graphics;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Factory to create a {@link Graphic} instance suited to create a file.
 *
 * @author hneemann
 */
public interface ExportFactory {
    /**
     * Creates a {@link Graphic} instance
     *
     * @param out the stream to write the graphic to
     * @param min upper right corner of the circuit
     * @param max lower left corner of the circuit
     * @return the {@link Graphic} instance to use
     * @throws IOException IOException
     */
    Graphic create(OutputStream out, Vector min, Vector max) throws IOException;
}
