package de.neemann.digital.analyse.quinemc.primeselector;

import de.neemann.digital.analyse.quinemc.TableRow;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * @author hneemann
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
