/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc.primeselector;


import de.neemann.digital.analyse.quinemc.TableRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

/**
 */
public class BruteForceGetAll implements PrimeSelector {

    private ArrayList<ArrayList<TableRow>> foundSolutions;

    @Override
    public void select(ArrayList<TableRow> primes, ArrayList<TableRow> primesAvail, TreeSet<Integer> termIndices) {
        if (primesAvail.size() > 31)
            throw new RuntimeException("to many primes");

        int comb = 1 << primesAvail.size();
        ArrayList<Integer> list = new ArrayList<>(comb);
        for (int i = 1; i < comb; i++) {
            list.add(i);
        }
        Collections.sort(list, (i1, i2) -> Integer.bitCount(i1) - Integer.bitCount(i2));

        int primesUsed = 0;

        foundSolutions = new ArrayList<>();

        ArrayList<Integer> indicesOpen = new ArrayList<>();
        for (int mask : list) {

            if (primesUsed != 0 && Integer.bitCount(mask) > primesUsed)
                break;

            indicesOpen.clear();
            indicesOpen.addAll(termIndices);
            int m = mask;
            for (TableRow aPrimesAvail : primesAvail) {
                if ((m & 1) > 0) {
                    indicesOpen.removeAll(aPrimesAvail.getSource());
                }
                m >>= 1;
            }
            if (indicesOpen.isEmpty()) {
                primesUsed = Integer.bitCount(mask);

                ArrayList<TableRow> singleSolution = new ArrayList<>(primes);
                m = mask;
                for (TableRow aPrime : primesAvail) {
                    if ((m & 1) > 0) {
                        singleSolution.add(aPrime);
                    }
                    m >>= 1;
                }
                foundSolutions.add(singleSolution);
            }
        }
        primes.clear();
        primes.addAll(foundSolutions.get(0));
    }

    @Override
    public ArrayList<ArrayList<TableRow>> getAllSolutions() {
        return foundSolutions;
    }
}
