package de.neemann.digital.integration;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.RAMSinglePort;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelDescription;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.LibrarySelector;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author hneemann
 */
public class TestProcessor extends TestCase {

    private Model createModel(String file) throws IOException, PinException, NodeException {
        File filename = new File(Resources.getRoot(), file);
        ElementLibrary library = new ElementLibrary();
        ShapeFactory shapeFactory = new ShapeFactory(library);
        Circuit circuit = Circuit.loadCircuit(filename, shapeFactory);
        LibrarySelector librarySelector = new LibrarySelector(library, shapeFactory, null);
        librarySelector.setFilePath(filename.getParentFile());

        ModelDescription md = new ModelDescription(circuit, library);
        Model model = md.createModel();
        model.init(true);

        assertTrue(model.isFastRunModel());
        return model;
    }

    /**
     * Loads a simulated processor, which has already stored machine program that calculates the 15th
     * fibonacci number with a simple recursive algorithm. The result (610) is stored in the first RAM word.
     *
     * @throws IOException   IOException
     * @throws NodeException NodeException
     * @throws PinException  PinException
     */
    public void testFibonacci() throws IOException, NodeException, PinException {
        Model model = createModel("dig/processor/Processor_fibonacci.dig");

        assertEquals(98644, model.runToBreak()); // checks the number of cycles needed to calculate 610

        List<RAMSinglePort> ramList = model.findNode(RAMSinglePort.class);
        assertEquals(1, ramList.size());

        DataField ram = ramList.get(0).getMemory(); // the rams content
        assertEquals(610, ram.getData(0));
    }


}
