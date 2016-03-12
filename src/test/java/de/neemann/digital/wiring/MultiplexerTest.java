package de.neemann.digital.wiring;

import de.neemann.digital.Model;
import de.neemann.digital.ObservableValue;
import de.neemann.digital.TestExecuter;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class MultiplexerTest extends TestCase {

    public void testMux() throws Exception {
        Model model = new Model();
        ObservableValue a = new ObservableValue(4);
        ObservableValue b = new ObservableValue(4);
        ObservableValue c = new ObservableValue(4);
        ObservableValue d = new ObservableValue(4);
        ObservableValue sel = new ObservableValue(2);
        ObservableValue out = model.add(new Multiplexer(4, sel)).addInput(a).addInput(b).addInput(c).addInput(d).getOutput();


        TestExecuter te = new TestExecuter(model).setInputs(a, b, c, d, sel).setOutputs(out);
        te.check(3, 4, 5, 6, 0, 3);
        te.check(3, 4, 5, 6, 1, 4);
        te.check(3, 4, 5, 6, 2, 5);
        te.check(3, 4, 5, 6, 3, 6);

    }
}