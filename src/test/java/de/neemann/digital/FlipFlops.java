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
        ObservableValue r = new ObservableValue("r", 1);
        ObservableValue s = new ObservableValue("s", 1);

        Model model = new Model();
        FanIn a1 = model.add(new NOr(1));
        FanIn a2 = model.add(new NOr(1));

        a1.setInputs(r, a2.getOutput());
        a2.setInputs(s, a1.getOutput());

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
        ObservableValue r = new ObservableValue("r", 1);
        ObservableValue s = new ObservableValue("s", 1);

        Model model = new Model();
        FanIn a1 = model.add(new NAnd(1));
        FanIn a2 = model.add(new NAnd(1));

        a1.setInputs(r, a2.getOutput());
        a2.setInputs(s, a1.getOutput());

        TestExecuter sc = new TestExecuter(model).setInputs(r, s).setOutputs(a1.getOutput(), a2.getOutput());
        sc.check(1, 0, 0, 1);
        sc.check(1, 1, 0, 1);
        sc.check(0, 1, 1, 0);
        sc.check(1, 1, 1, 0);
        sc.check(1, 0, 0, 1);
    }


    public void testFlipFlopJKMS() throws Exception {
        ObservableValue j = new ObservableValue("j", 1);
        ObservableValue k = new ObservableValue("k", 1);
        ObservableValue c = new ObservableValue("c", 1);


        Model model = new Model();
        FanIn nor3 = model.add(new NOr(1));
        FanIn nor4 = model.add(new NOr(1));

        FanIn a1 = model.add(new And(1));
        a1.setInputs(j, c, nor4.getOutput());
        FanIn a2 = model.add(new And(1));
        a2.setInputs(k, c, nor3.getOutput());
        Not not = model.add(new Not(1));
        not.setInputs(c);

        FanIn nor1 = model.add(new NOr(1));
        FanIn nor2 = model.add(new NOr(1));

        nor1.setInputs(a1.getOutput(), nor2.getOutput());
        nor2.setInputs(a2.getOutput(), nor1.getOutput());


        FanIn a3 = model.add(new And(1));
        a3.setInputs(nor1.getOutput(), not.getOutput());
        FanIn a4 = model.add(new And(1));
        a4.setInputs(nor2.getOutput(), not.getOutput());

        nor3.setInputs(a3.getOutput(), nor4.getOutput());
        nor4.setInputs(a4.getOutput(), nor3.getOutput());

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