/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Operation;
import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import java.util.Map;

public class CleanNameBuilderTest extends TestCase {

    public void testSimple() throws BuilderException {
        BuilderCollector bc = new BuilderCollector();
        CleanNameBuilder cnb = new CleanNameBuilder(bc);

        String n0 = "z###0";
        String n1 = "z#'#0";

        cnb.addCombinatorial(n0, Operation.and(new Variable(n0), new Variable(n1)));
        cnb.addCombinatorial(n1, Operation.or(new Variable(n0), new Variable(n1)));

        Map<String, Expression> comb = bc.getCombinatorial();
        assertEquals(2, comb.size());
        assertEquals("and(z0,z01)", comb.get("z0").toString());
        assertEquals("or(z0,z01)", comb.get("z01").toString());
    }

    public void testSimple2() throws BuilderException {
        BuilderCollector bc = new BuilderCollector();
        CleanNameBuilder cnb = new CleanNameBuilder(bc);

        String n0 = "z_0^n";
        String n1 = "z_1^n";

        cnb.addCombinatorial(n0, Operation.and(new Variable(n0), new Variable(n1)));
        cnb.addCombinatorial(n1, Operation.or(new Variable(n0), new Variable(n1)));

        Map<String, Expression> comb = bc.getCombinatorial();
        assertEquals(2, comb.size());
        assertEquals("and(z_0n,z_1n)", comb.get("z_0n").toString());
        assertEquals("or(z_0n,z_1n)", comb.get("z_1n").toString());
    }

    public void testEmpty() throws BuilderException {
        BuilderCollector bc = new BuilderCollector();
        CleanNameBuilder cnb = new CleanNameBuilder(bc, name -> null);

        String n0 = "z_0^n";
        String n1 = "z_1^n";

        cnb.addCombinatorial(n0, Operation.and(new Variable(n0), new Variable(n1)));
        cnb.addCombinatorial(n1, Operation.or(new Variable(n0), new Variable(n1)));

        Map<String, Expression> comb = bc.getCombinatorial();
        assertEquals(2, comb.size());
        assertEquals("and(X,X1)", comb.get("X").toString());
        assertEquals("or(X,X1)", comb.get("X1").toString());
    }

    public void testSequential() throws BuilderException {
        BuilderCollector bc = new BuilderCollector();
        CleanNameBuilder cnb = new CleanNameBuilder(bc);

        String n0 = "z_0^n";
        String n1 = "z_1^n";

        cnb.addSequential(n0 + "+1", Operation.and(new Variable(n0), new Variable(n1)));
        cnb.addSequential(n1 + "+1", Operation.or(new Variable(n0), new Variable(n1)));

        Map<String, Expression> reg = bc.getRegistered();
        assertEquals(2, reg.size());
        assertEquals("and(z_0n,z_1n)", reg.get("z_0n1").toString());
        assertEquals("or(z_0n,z_1n)", reg.get("z_1n1").toString());
    }
}