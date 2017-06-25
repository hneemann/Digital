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
     * @return the {@link Graphic} instance to use
     * @throws IOException IOException
     */
    Graphic create(OutputStream out) throws IOException;
}
