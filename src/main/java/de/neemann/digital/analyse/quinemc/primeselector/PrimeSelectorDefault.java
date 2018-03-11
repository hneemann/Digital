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
 */
public class PrimeSelectorDefault implements PrimeSelector {

    private PrimeSelector ps;

    @Override
    public void select(ArrayList<TableRow> primes, ArrayList<TableRow> primesAvail, TreeSet<Integer> termIndices) {
        int count = primesAvail.size();
        if (count <= 22) {
            ps = new BruteForceGetAll();
        } else {
            ps = new LargestFirst();
        }
        ps.select(primes, primesAvail, termIndices);
    }

    @Override
    public ArrayList<ArrayList<TableRow>> getAllSolutions() {
        if (ps == null)
            return null;
        else
            return ps.getAllSolutions();
    }

}
