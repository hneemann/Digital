/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;

import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;

/**
 */
public class QuineMcCluskeyExactCoverTest extends TestCase {


    public void testExactCoverLoop() {
        ArrayList<Variable> vars = new ArrayList<>();
        vars.add(new Variable("a"));
        vars.add(new Variable("b"));
        vars.add(new Variable("c"));
        vars.add(new Variable("d"));
        ArrayList<TableRow> primes = new ArrayList<>();
        primes.add(new TableRow(9, 0).addSource(1,3,5,7,9));
        primes.add(new TableRow(9, 1).addSource(0,1,2,3,5,6,7,8,9));
        primes.add(new TableRow(9, 2).addSource(0,2,3,5,8,9));
        primes.add(new TableRow(9, 3).addSource(1,2,4,6,8,9));
        primes.add(new TableRow(9, 4).addSource(0,1,4,5,8,9));
        primes.add(new TableRow(9, 5).addSource(2,3,6,8,9));
        primes.add(new TableRow(9, 6).addSource(2,4,5,7,9));
        primes.add(new TableRow(9, 7).addSource(0,1,3,6,7,9));


        QuineMcCluskey qmc = new QuineMcCluskey(vars, null, primes);
        qmc.simplifyPrimes(null);
        final ArrayList<TableRow> pri = qmc.getPrimes();
        assertEquals(2, pri.size());

        Collection<Integer> s1 = pri.get(0).getSource();
        assertEquals(1, s1.size());
        assertTrue(s1.contains(3));

        Collection<Integer> s2 = pri.get(1).getSource();
        assertEquals(1, s2.size());
        assertTrue(s2.contains(4));
    }

}
