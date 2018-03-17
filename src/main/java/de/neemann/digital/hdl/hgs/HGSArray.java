/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

/**
 * An Array accessible from the template engine
 */
public interface HGSArray {

    /**
     * @return the size of the array
     * @throws HGSEvalException HGSEvalException
     */
    int hgsArraySize() throws HGSEvalException;

    /**
     * Adds a value to the array
     *
     * @param initial value
     * @throws HGSEvalException HGSEvalException
     */
    default void hgsArrayAdd(Object initial) throws HGSEvalException {
        throw new HGSEvalException("Array growth is not supported!");
    }


    /**
     * Sets a value to the array.
     * Index will never exceed the array bounds!
     *
     * @param i   index
     * @param val value
     * @throws HGSEvalException HGSEvalException
     */
    default void hgsArraySet(int i, Object val) throws HGSEvalException {
        throw new HGSEvalException("It's not allowed to set a value to this array");
    };

    /**
     * Gets a value from the array.
     * Index will never exceed the array bounds!
     *
     * @param i index, always greater or equals zero and less than size
     * @return the value
     * @throws HGSEvalException HGSEvalException
     */
    Object hgsArrayGet(int i) throws HGSEvalException;

}


