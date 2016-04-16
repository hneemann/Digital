package de.neemann.digital.integration;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.linemerger.GraphicLineCollector;
import de.neemann.digital.draw.graphics.linemerger.GraphicSkipLines;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Only checks that something is written without an error
 *
 * @author hneemann
 */
public class TestExport extends TestCase {

    private interface Creator {
        Graphic create(OutputStream out, Vector min, Vector max) throws IOException;
    }

    private static ByteArrayOutputStream export(String file, Creator creator) throws NodeException, PinException, IOException {
        Circuit circuit = new ToBreakRunner(file).getCircuit();

        GraphicMinMax minMax = new GraphicMinMax();
        circuit.drawTo(minMax);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Graphic gr = creator.create(baos, minMax.getMin(), minMax.getMax());

        GraphicLineCollector glc = new GraphicLineCollector();
        circuit.drawTo(glc);
        glc.drawTo(gr);

        circuit.drawTo(new GraphicSkipLines(gr));
        return baos;
    }

    public void testSVGExport() throws NodeException, PinException, IOException {
        ByteArrayOutputStream baos
                = export("dig/processor/Processor_fibonacci.dig",
                (out, min, max) -> new GraphicSVGIndex(out, min, max, null, 15));

        assertTrue(baos.size() > 15000);
    }

    public void testSVGExportLaTeX() throws NodeException, PinException, IOException {
        ByteArrayOutputStream baos
                = export("dig/processor/Processor_fibonacci.dig",
                (out, min, max) -> new GraphicSVGLaTeX(out, min, max, null, 15));

        assertTrue(baos.size() > 8000);
    }

}
