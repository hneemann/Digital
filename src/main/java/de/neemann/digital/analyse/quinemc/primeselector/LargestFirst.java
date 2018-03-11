/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc.primeselector;


import de.neemann.digital.analyse.quinemc.TableRow;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Tries at first the primes containing the most indices
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
