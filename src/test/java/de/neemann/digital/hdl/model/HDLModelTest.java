package de.neemann.digital.hdl.model;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class HDLModelTest extends TestCase {

    public void testXor() throws PinException, NodeException, ElementNotFoundException, IOException, HDLException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/xor.dig");
        HDLModel model = new HDLModel(br.getCircuit(), br.getLibrary(), new ModelList(br.getLibrary()));

        assertEquals(5, model.size());
        assertEquals(7, model.getSignals().size());
        assertEquals(3, model.getPorts().size());
    }

    public void testXorNeg() throws PinException, NodeException, ElementNotFoundException, IOException, HDLException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/xorNeg.dig");
        HDLModel model = new HDLModel(br.getCircuit(), br.getLibrary(), new ModelList(br.getLibrary()));

        assertEquals(5, model.size());
        assertEquals(7, model.getSignals().size());
        assertEquals(3, model.getPorts().size());
    }

    public void testXorNegBus() throws PinException, NodeException, ElementNotFoundException, IOException, HDLException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/xorNegBus.dig");
        HDLModel model = new HDLModel(br.getCircuit(), br.getLibrary(), new ModelList(br.getLibrary()));

        assertEquals(5, model.size());
        assertEquals(7, model.getSignals().size());
        assertEquals(3, model.getPorts().size());
    }

    public void testNegUsages() throws PinException, NodeException, ElementNotFoundException, IOException, HDLException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/neg.dig");
        HDLModel model = new HDLModel(br.getCircuit(), br.getLibrary(), new ModelList(br.getLibrary()));

        assertEquals(2, model.size());
        assertEquals(3, model.getSignals().size());
        assertEquals(2, model.getPorts().size());
    }

    public void testSplitter() throws PinException, NodeException, ElementNotFoundException, IOException, HDLException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/splitter.dig");
        HDLModel model = new HDLModel(br.getCircuit(), br.getLibrary(), new ModelList(br.getLibrary()));

        assertEquals(1, model.size());
        assertEquals(4, model.getSignals().size());
        assertEquals(4, model.getPorts().size());
    }


    public void testClock() throws PinException, NodeException, ElementNotFoundException, IOException, HDLException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/Clock.dig");
        HDLModel model = new HDLModel(br.getCircuit(), br.getLibrary(), new ModelList(br.getLibrary()));
        new ClockIntegratorGeneric(10).integrateClocks(model);

        assertEquals(2, model.size());
        assertEquals(3, model.getSignals().size());
        assertEquals(2, model.getPorts().size());
    }

    public void testClock2() throws PinException, NodeException, ElementNotFoundException, IOException, HDLException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/Clock2.dig");
        HDLModel model = new HDLModel(br.getCircuit(), br.getLibrary(), new ModelList(br.getLibrary()));
        new ClockIntegratorGeneric(10).integrateClocks(model);

        assertEquals(2, model.size());
        assertEquals(4, model.getSignals().size());
        assertEquals(3, model.getPorts().size());
    }

}