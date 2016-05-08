package de.neemann.digital.analyse.quinemc;

/**
 * A simple bool table
 *
 * @author hneemann
 */
public interface BoolTable {
    /**
     * @return the table row count
     */
    int size();

    /**
     * returns the value at the given row
     *
     * @param i the index
     * @return the value
     */
    ThreeStateValue get(int i);
}
