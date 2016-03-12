package de.neemann.digital;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.basic.*;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class FlipFlops extends TestCase {

    public void testFlipFlopNOr() throws Exception {
        ObservableValue r = new ObservableValue(1);
        ObservableValue s = new ObservableValue(1);

        Model model = new Model();
        FanIn a1 = model.add(new NOr(1)).addInput(r);
        FanIn a2 = model.add(new NOr(1)).addInput(s);

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
        FanIn a1 = model.add(new NAnd(1)).addInput(r);
        FanIn a2 = model.add(new NAnd(1)).addInput(s);

        a1.addInput(a2.getOutput());
        a2.addInput(a1.getOutput());

        TestExecuter sc = new TestExecuter(model).setInputs(r, s).setOutputs(a1.getOutput(), a2.getOutput());
        sc.check(1, 0, 0, 1);
        sc.check(1, 1, 0, 1);
        sc.check(0, 1, 1, 0);
        sc.check(1, 1, 1, 0);
        sc.check(1, 0, 0, 1);
    }


    public void testFlipFlopJKMS() throws Exception {
        ObservableValue j = new ObservableValue(1);
        ObservableValue k = new ObservableValue(1);
        ObservableValue c = new ObservableValue(1);

        Model model = new Model();
        FanIn a1 = model.add(new And(1)).addInput(j).addInput(c);
        FanIn a2 = model.add(new And(1)).addInput(k).addInput(c);
        Not not = model.add(new Not(c));

        FanIn nor1 = model.add(new NOr(1)).addInput(a1.getOutput());
        FanIn nor2 = model.add(new NOr(1)).addInput(a2.getOutput());

        nor1.addInput(nor2.getOutput());
        nor2.addInput(nor1.getOutput());


        FanIn a3 = model.add(new And(1)).addInput(nor1.getOutput()).addInput(not.getOutput());
        FanIn a4 = model.add(new And(1)).addInput(nor2.getOutput()).addInput(not.getOutput());

        FanIn nor3 = model.add(new NOr(1)).addInput(a3.getOutput());
        FanIn nor4 = model.add(new NOr(1)).addInput(a4.getOutput());

        nor3.addInput(nor4.getOutput());
        nor4.addInput(nor3.getOutput());

        a1.addInput(nor4.getOutput());
        a2.addInput(nor3.getOutput());

        TestExecuter sc = new TestExecuter(model, true).setInputs(c, j, k).setOutputs(nor3.getOutput(), nor4.getOutput());
        sc.check(0, 1, 0, -1, -1); // undefined
        sc.check(1, 1, 0, -1, -1); // undefined
        sc.check(0, 1, 0, 1, 0);
        sc.check(0, 0, 0, 1, 0);
        sc.check(1, 0, 0, 1, 0);
        sc.check(0, 1, 0, 1, 0);
        sc.check(1, 1, 0, 1, 0);
        sc.check(0, 0, 0, 1, 0);
        sc.check(1, 0, 0, 1, 0);
        sc.check(0, 0, 1, 1, 0);
        sc.check(1, 0, 1, 1, 0);
        sc.check(0, 0, 1, 0, 1);

        sc.check(0, 1, 1, 0, 1);
        sc.check(1, 1, 1, 0, 1);
        sc.check(0, 1, 1, 1, 0);
        sc.check(1, 1, 1, 1, 0);
        sc.check(0, 1, 1, 0, 1);
        sc.check(1, 1, 1, 0, 1);
        sc.check(0, 1, 1, 1, 0);
        sc.check(1, 1, 1, 1, 0);
        sc.check(0, 1, 1, 0, 1);
    }
}