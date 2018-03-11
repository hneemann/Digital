/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Replaces input and outputs by small circles
 */
public class GraphicSVGLaTeXInOut extends GraphicSVGLaTeX {

    /**
     * Creates a new instance
     *
     * @param out the stream to write the data to
     * @throws IOException IOException
     */
    public GraphicSVGLaTeXInOut(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    public boolean isFlagSet(String name) {
        return name.equals(LATEX);
    }

}
