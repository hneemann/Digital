package de.neemann.digital.core.memory;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

import static de.neemann.digital.TestExecuter.HIGHZ;
import static de.neemann.digital.core.ObservableValues.ovs;

/**
 * @author hneemann
 */
public class RAMTripleAccessTest extends TestCase {

    public void testRAM() throws Exception {
        ObservableValue str = new ObservableValue("str", 1);
        ObservableValue clk = new ObservableValue("clk", 1);
        ObservableValue a1  = new ObservableValue("a1",  4);
        ObservableValue a2  = new ObservableValue("a2",  4);
        ObservableValue d   = new ObservableValue("d",   4);
        ObservableValue a   = new ObservableValue("a",   4);

        Model model = new Model();
        RAMTripleAccess out = model.add(new RAMTripleAccess(
                new ElementAttributes()
                        .set(Keys.ADDR_BITS, 4)
                        .setBits(4)));
        out.setInputs(ovs(str, clk, a1, a2, d, a));

        TestExecuter sc = new TestExecuter(model).setInputs(str, clk, a1, a2, d, a).setOutputs(out.getOutputs());
        //       STR CLK A1 A2 D  A
        sc.check(0,  0,  0, 0, 0, 0, 0, 0);  //            RD A1[0]=0, RD A2[0]=0
        sc.check(1,  1,  0, 0, 3, 0, 3, 3);  // WR A[0]=3, RD A1[0]=3, RD A2[0]=3
        sc.check(0,  0,  0, 1, 0, 0, 3, 0);  //            RD A1[0]=3, RD A2[1]=0
        sc.check(1,  1,  1, 0, 9, 1, 9, 3);  // WR A[1]=9, RD A1[1]=9, RD A2[0]=3
        sc.check(0,  0,  5, 0, 1, 0, 0, 3);  //            RD A1[5]=0, RD A2[0]=3
        sc.check(1,  0,  0, 5, 1, 0, 3, 0);  // WR A[0]=1, RD A1[0]=1, RD A2[5]=0
    }
}