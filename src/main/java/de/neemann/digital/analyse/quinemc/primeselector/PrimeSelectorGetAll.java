package de.neemann.digital.analyse.quinemc.primeselector;


import de.neemann.digital.analyse.quinemc.TableRow;

import java.util.ArrayList;

/**
 * Used to create all possible sollutions
 *
 * @author hneemann
 */
public interface PrimeSelectorGetAll {

    /**
     * @return all possible solutions
     */
    ArrayList<ArrayList<TableRow>> getAllSolutions();
}
