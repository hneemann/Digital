package de.neemann.digital.draw.graphics;

import java.io.File;
import java.io.IOException;

/**
 * @author hneemann
 */
public interface Exporter {
    Graphic create(File file, Vector min, Vector max) throws IOException;
}
