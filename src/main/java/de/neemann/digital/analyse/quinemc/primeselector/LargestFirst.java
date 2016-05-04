package de.neemann.digital.analyse.quinemc.primeselector;


import de.neemann.digital.analyse.quinemc.TableRow;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Tries at first the primes containing the most indices
 *
 * @author hneemann
 */
public class LargestFirst implements PrimeSelector {
    @Override
    public void select(ArrayList<TableRow> primes, ArrayList<TableRow> primesAvail, TreeSet<Integer> termIndices) {
        while (!termIndices.isEmpty()) {
            TableRow bestRow = null;
            int maxCount = 0;
            for (TableRow tr : primesAvail) {
                int count = 0;
                for (int i : tr.getSource()) {
                    if (termIndices.contains(i))
                        count++;
                }
                if (count > maxCount) {
                    maxCount = count;
                    bestRow = tr;
                }
            }
            primes.add(bestRow);
            primesAvail.remove(bestRow);
            termIndices.removeAll(bestRow.getSource());
        }
    }
}
