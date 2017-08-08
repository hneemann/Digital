package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class VHDLExporterTest extends TestCase {

    public void testXor() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/xor.dig");
        String vhdl = new VHDLExporter(br.getLibrary()).export(br.getCircuit()).toString();
        System.out.println(vhdl);
    }

    public void testXorNeg() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/xorNeg.dig");
        String vhdl = new VHDLExporter(br.getLibrary()).export(br.getCircuit()).toString();
        System.out.println(vhdl);
    }

    public void testNeg() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/neg.dig");
        String vhdl = new VHDLExporter(br.getLibrary()).export(br.getCircuit()).toString();
        System.out.println(vhdl);
    }

}