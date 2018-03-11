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
 * Represents an algorithm which chooses the final primes
 */
public interface PrimeSelector {

    /**
     * Selects the primes to use
     *
     * @param primes      the list to add the primes to
     * @param primesAvail the available primes
     * @param termIndices the indices
     */
    void select(ArrayList<TableRow> primes, ArrayList<TableRow> primesAvail, TreeSet<Integer> termIndices);

    /**
     * @return all possible solutions
     */
    default ArrayList<ArrayList<TableRow>> getAllSolutions() {
        return null;
    }

}
