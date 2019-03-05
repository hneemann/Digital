/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Not as efficient as {@link QuineMcCluskey} but it shows all possible solutions.
 * Needed for exam correction.
 * It does not throw away all primes which are not necessary but tries to find the primes
 * which are necessary. So is is possible to find all possible solutions.
 * Works only if there are not more than 4 variables.
 * <p>
 */
public class QuineMcCluskeyExam extends QuineMcCluskey {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuineMcCluskeyExam.class);

    /**
     * Creates a new instance
     *
     * @param variables the variables to use
     */
    public QuineMcCluskeyExam(List<Variable> variables) {
        super(variables);
    }

    /**
     * Simplify the primes
     *
     * @param primeSelector the prime selector to use
     */
    @Override
    public void simplifyPrimes(PrimeSelector primeSelector) {
        ArrayList<TableRow> primes = getPrimes();
        ArrayList<TableRow> primesAvail = new ArrayList<>(primes);
        primes.clear();

        TreeSet<Integer> termIndices = new TreeSet<>();
        for (TableRow r : primesAvail)
            termIndices.addAll(r.getSource());

        // Nach primtermen suchen, welche einen index exclusiv enthalten
        // Diese müssen in jedem Falle enthalten sein!
        for (int pr : termIndices) {

            TableRow foundPrime = null;
            for (TableRow tr : primesAvail) {
                if (tr.getSource().contains(pr)) {
                    if (foundPrime == null) {
                        foundPrime = tr;
                    } else {
                        foundPrime = null;
                        break;
                    }
                }
            }

            if (foundPrime != null) {
                if (!primes.contains(foundPrime))
                    primes.add(foundPrime);
            }
        }
        primesAvail.removeAll(primes);

        // Die, Indices die wir schon haben können raus;
        for (TableRow pr : primes) {
            termIndices.removeAll(pr.getSource());
        }

        LOGGER.debug("residual primes " + primesAvail.size());

        if (!termIndices.isEmpty()) {

            //Die noch übrigen Terme durchsuchen ob sie schon komplett dabei sind;
            Iterator<TableRow> it = primesAvail.iterator();
            while (it.hasNext()) {
                TableRow tr = it.next();
                boolean needed = false;
                for (int i : tr.getSource()) {
                    if (termIndices.contains(i)) {
                        needed = true;
                        break;
                    }
                }
                if (!needed) {
                    it.remove();
                }
            }

            primeSelector.select(primes, primesAvail, termIndices);
        }
        LOGGER.debug("final primes " + primes.size());
    }

}
