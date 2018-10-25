/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.arithmetic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

import static de.neemann.digital.core.ObservableValues.ovs;

public class DivTest extends TestCase {

    public void testDivUnsigned() throws Exception {
        ObservableValue av = new ObservableValue("a", 8);
        ObservableValue bv = new ObservableValue("b", 8);


        Model model = new Model();
        Div node = model.add(new Div(new ElementAttributes().setBits(8).set(Keys.SIGNED, false)));
        node.setInputs(ovs(av, bv));

        TestExecuter sc = new TestExecuter(model).setInputs(av, bv).setOutputsOf(node);
        for (int a = 0; a < 256; a++)
            for (int b = 1; b < 256; b++) {
                Result r = divide_unsigned(a, b);
                sc.check(a, b, r.Q, r.R);
            }

    }

    public void testDivSigned() throws Exception {
        ObservableValue av = new ObservableValue("a", 8);
        ObservableValue bv = new ObservableValue("b", 8);


        Model model = new Model();
        Div node = model.add(new Div(new ElementAttributes().setBits(8).set(Keys.SIGNED, true)));
        node.setInputs(ovs(av, bv));

        TestExecuter sc = new TestExecuter(model).setInputs(av, bv).setOutputsOf(node);

        for (int a = -128; a < 128; a++)
            for (int b = -128; b < 128; b++) {
                if (b!=0) {
                    Result r = divide(a, b);
                    sc.check(a, b, r.Q, r.R);
                }
            }

    }



    private static final class Result {
        private int Q;
        private int R;

        private Result(int Q, int R) {
            this.Q = Q;
            this.R = R;
        }
    }


    // see: https://en.wikipedia.org/wiki/Division_algorithm
    private static Result divide(int N, int D) {
        if (D == 0) throw new RuntimeException("DivisionByZero");
        if (D < 0) {
            Result r = divide(N, -D);
            return new Result(-r.Q, r.R);
        }
        if (N < 0) {
            Result r = divide(-N, D);
            if (r.R == 0) return new Result(-r.Q, 0);
            else return new Result(-r.Q - 1, D - r.R);
        }
        // At this point, N ≥0 and D >0
        return divide_unsigned(N, D);
    }

    private static Result divide_unsigned(int N, int D) {
        if (D == 0) throw new RuntimeException("DivisionByZero");
        int Q = 0;
        int R = N;
        while (R >= D) {
            Q = Q + 1;
            R = R - D;
        }
        return new Result(Q, R);
    }

}