package de.neemann.digital.analyse.quinemc.primeselector;


import de.neemann.digital.analyse.quinemc.TableRow;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Represents an algorithm which chooses the final primes
 *
 * @author hneemann
 */
public interface PrimeSelector {

    /**
     * The default prime selector
     */
    PrimeSelector DEFAULT = new PrimeSelector() {

        private final PrimeSelector bruteForce = new BruteForceGetAll();
        private final PrimeSelector largestFirst = new LargestFirst();

        @Override
        public void select(ArrayList<TableRow> primes, ArrayList<TableRow> primesAvail, TreeSet<Integer> termIndices) {
            int count = primesAvail.size();

            if (count <= 12) {
                bruteForce.select(primes, primesAvail, termIndices);
            } else {
                largestFirst.select(primes, primesAvail, termIndices);
            }
        }
    };

    /**
     * Selects the primes to use
     *
     * @param primes      the list to add the primes to
     * @param primesAvail the available promes
     * @param termIndices the indices
     */
    void select(ArrayList<TableRow> primes, ArrayList<TableRow> primesAvail, TreeSet<Integer> termIndices);
}
