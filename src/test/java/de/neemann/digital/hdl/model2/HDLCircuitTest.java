package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.integration.Resources;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class HDLCircuitTest extends TestCase {

    public void testSimple() throws IOException, PinException, HDLException, NodeException {
        File file = new File(Resources.getRoot(), "dig/hdl/model2/comb.dig");
        ElementLibrary library = new ElementLibrary();
        library.setRootFilePath(file.getParentFile());
        ShapeFactory shapeFactory = new ShapeFactory(library);
        Circuit c = Circuit.loadCircuit(file, shapeFactory);

        final HDLContext hdlContext = new HDLContext(library);
        HDLCircuit hdl = new HDLCircuit(c, "main", hdlContext);

        CodePrinterStr out = new CodePrinterStr();
        for (HDLCircuit cir : hdlContext) {
            cir.mergeOperations();
            cir.print(out);
            out.println();
        }

        hdl.mergeOperations().nameNets(new HDLCircuit.NetNamer() {
            private int num;

            @Override
            public String createName(HDLNet n) {
                return "s" + (num++);
            }
        });
        hdl.print(out);

        System.out.print(out);
        System.out.println();
    }

}