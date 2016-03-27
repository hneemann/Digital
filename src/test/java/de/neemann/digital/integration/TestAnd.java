package de.neemann.digital.integration;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelBuilder;
import de.neemann.digital.draw.model.ModelEntry;
import junit.framework.TestCase;

import java.io.File;
import java.util.List;

/**
 * @author hneemann
 */
public class TestAnd extends TestCase {

    private ElementLibrary library = new ElementLibrary();

    /**
     * Reads a file and sets up a model from it.
     * After that the model - a simple AND gate - is tested to be a working AND gate.
     *
     * @throws Exception
     */
    public void testAnd() throws Exception {
        File filename = new File(Resources.getRoot(), "dig/and.dig");
        Circuit circuit = Circuit.loadCircuit(filename);

        ModelBuilder mb = new ModelBuilder(circuit);
        Model model = mb.build(library);

        List<Node> nodes = model.getNodes();
        assertEquals(1, nodes.size());

        // get inputs and outputs
        List<ModelEntry> inputs = mb.getModelDescription().getEntries("In");
        assertEquals(2, inputs.size());
        List<ModelEntry> outputs = mb.getModelDescription().getEntries("Out");
        assertEquals(1, outputs.size());

        // check the inputs state: the input itself has an output
        assertEquals(0, inputs.get(0).getIoState().inputCount());
        assertEquals(1, inputs.get(0).getIoState().outputCount());
        assertEquals(0, inputs.get(1).getIoState().inputCount());
        assertEquals(1, inputs.get(1).getIoState().outputCount());

        // check the output state: the output itself has an input
        assertEquals(1, outputs.get(0).getIoState().inputCount());
        assertEquals(0, outputs.get(0).getIoState().outputCount());

        // setup the test executer
        TestExecuter te = new TestExecuter(model).setInputs(inputs).setOutputs(outputs);

        te.check(0, 0, 0);
        te.check(0, 1, 0);
        te.check(1, 0, 0);
        te.check(1, 1, 1);
    }

    /**
     * Same test as above written more simple
     *
     * @throws Exception
     */
    public void testAnd2() throws Exception {
        TestExecuter te = TestExecuter.createFromFile("dig/and.dig", library);

        te.check(0, 0, 0);
        te.check(0, 1, 0);
        te.check(1, 0, 0);
        te.check(1, 1, 1);

        // only a single And-Node
        assertEquals(1, te.getModel().getNodes().size());

        // every calculation needs a single micro step
        assertEquals(4, te.getModel().getStepCounter());
    }

}
