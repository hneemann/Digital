package de.neemann.digital.integration;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.GraphicSVGIndex;
import de.neemann.digital.draw.graphics.GraphicSVGLaTeX;
import de.neemann.digital.draw.graphics.linemerger.GraphicLineCollector;
import de.neemann.digital.draw.graphics.linemerger.GraphicSkipLines;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Only checks that something is written without an error
 *
 * @author hneemann
 */
public class TestExport extends TestCase {

    public void testSVGExport() throws NodeException, PinException, IOException {
        Circuit circuit = new ToBreakRunner("dig/processor/Processor_fibonacci.dig").getCircuit();

        GraphicMinMax minMax = new GraphicMinMax();
        circuit.drawTo(minMax);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GraphicSVGIndex gr = new GraphicSVGIndex(baos, minMax.getMin(), minMax.getMax(), null, 15);

        GraphicLineCollector glc = new GraphicLineCollector();
        circuit.drawTo(glc);
        glc.drawTo(gr);

        circuit.drawTo(new GraphicSkipLines(gr));

        assertTrue(baos.size() > 15000);
    }

    public void testSVGExportLaTeX() throws NodeException, PinException, IOException {
        Circuit circuit = new ToBreakRunner("dig/processor/Processor_fibonacci.dig").getCircuit();

        GraphicMinMax minMax = new GraphicMinMax();
        circuit.drawTo(minMax);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GraphicSVGLaTeX gr = new GraphicSVGLaTeX(baos, minMax.getMin(), minMax.getMax(), null, 15);

        GraphicLineCollector glc = new GraphicLineCollector();
        circuit.drawTo(glc);
        glc.drawTo(gr);

        circuit.drawTo(new GraphicSkipLines(gr));

        assertTrue(baos.size() > 8000);
    }

}
