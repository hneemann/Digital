package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.integration.Resources;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class HDLCircuitTest extends TestCase {

    public void testSimple() throws IOException, PinException, HDLException, NodeException {
        File file = new File(Resources.getRoot(), "../../main/dig/processor/VHDLExample.dig");
        ElementLibrary library = new ElementLibrary();
        library.setRootFilePath(file.getParentFile());
        ShapeFactory shapeFactory = new ShapeFactory(library);
        Circuit c = Circuit.loadCircuit(file, shapeFactory);

        final HDLContext hdlContext = new HDLContext(library);
        HDLCircuit hdl = new HDLCircuit(c, "main", hdlContext);

        System.out.println(hdl);
        hdl.traverse(System.out::println);
    }

}