/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Export;
import de.neemann.digital.draw.graphics.GraphicsImage;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 */
public class TestShapes extends TestCase {

    /**
     * Loads a circuit with all available elements and writes it
     * to a PNG.
     *
     * @throws Exception
     */
    public void testShapes() throws Exception {
        useShapes(false);
    }

    public void testShapesIEEE() throws Exception {
        useShapes(true);
    }

    private void useShapes(boolean ieee) throws Exception {
        File filename = new File(Resources.getRoot(), "dig/shapes.dig");
        ElementLibrary library = new ElementLibrary();
        library.setRootFilePath(new File(Resources.getRoot(), "dig"));
        ShapeFactory shapeFactory = new ShapeFactory(library, ieee);
        Circuit circuit = Circuit.loadCircuit(filename, shapeFactory);

        // try to write circuit to graphics instance
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Export(circuit,
                (out) -> new GraphicsImage(out, "PNG", 1))
                .export(baos);

        assertTrue(baos.size() > 30000);

    }
}
