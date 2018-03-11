/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Factory to create a {@link Graphic} instance suited to create a file.
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
