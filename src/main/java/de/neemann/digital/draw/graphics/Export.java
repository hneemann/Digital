/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.linemerger.GraphicLineCollector;
import de.neemann.digital.draw.graphics.linemerger.GraphicSkipLines;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Helper to export graphics files.
 * Sets the bounding box of the graphic and collects single lines to a long stroke
 */
public class Export {

    private final Circuit circuit;
    private final ExportFactory factory;

    /**
     * Creates a new instance
     *
     * @param circuit the circuit to export
     * @param factory the factory to create the graphics instance
     */
    public Export(Circuit circuit, ExportFactory factory) {
        this.circuit = circuit;
        this.factory = factory;
    }

    /**
     * Export the file
     *
     * @param out stream to write the file to
     * @throws IOException IOException
     */
    public void export(OutputStream out) throws IOException {
        try (Graphic gr = factory.create(out)) {
            GraphicMinMax minMax = new GraphicMinMax(gr);
            circuit.drawTo(minMax);

            gr.setBoundingBox(minMax.getMin(), minMax.getMax());

            GraphicLineCollector glc = new GraphicLineCollector();
            circuit.drawTo(glc);
            glc.drawTo(gr);

            circuit.drawTo(new GraphicSkipLines(gr));
        }
    }
}
