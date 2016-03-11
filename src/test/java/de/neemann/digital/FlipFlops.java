package de.neemann.digital;

import de.neemann.digital.basic.Function;
import de.neemann.digital.basic.NAnd;
import de.neemann.digital.basic.NOr;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class FlipFlops extends TestCase {

    public void testFlipFlopNOr() throws Exception {
        ObservableValue r = new ObservableValue(1);
        ObservableValue s = new ObservableValue(1);

        Model model = new Model();
        Function a1 = model.add(new NOr(1)).addInput(r);
        Function a2 = model.add(new NOr(1)).addInput(s);

        a1.addInput(a2.getOutput());
        a2.addInput(a1.getOutput());

        TestExecuter sc = new TestExecuter(model, true).setInputs(r, s).setOutputs(a1.getOutput(), a2.getOutput());
        sc.check(0, 1, 1, 0);
        sc.check(0, 0, 1, 0);
        sc.check(1, 0, 0, 1);
        sc.check(0, 0, 0, 1);
        sc.check(0, 1, 1, 0);
        sc.check(1, 1, 0, 0);   // verbotener Zustand!!
        r.setValue(0);          // gehe aus verbotenem Zustand raus!!!
        s.setValue(0);
        model.doStep(true);     // geht nur mit noise!

        assertTrue((a1.getOutput().getValueBits() == 1 && a2.getOutput().getValueBits() == 0) ||  // endzustand ist undefiniert!
                (a1.getOutput().getValueBits() == 0 && a2.getOutput().getValueBits() == 1));
    }

    public void testFlipFlopNAnd() throws Exception {
        ObservableValue r = new ObservableValue(1);
        ObservableValue s = new ObservableValue(1);

        Model model = new Model();
        Function a1 = model.add(new NAnd(1)).addInput(r);
        Function a2 = model.add(new NAnd(1)).addInput(s);

        a1.addInput(a2.getOutput());
        a2.addInput(a1.getOutput());

        TestExecuter sc = new TestExecuter(model).setInputs(r, s).setOutputs(a1.getOutput(), a2.getOutput());
        sc.check(1, 0, 0, 1);
        sc.check(1, 1, 0, 1);
        sc.check(0, 1, 1, 0);
        sc.check(1, 1, 1, 0);
        sc.check(1, 0, 0, 1);
    }

}