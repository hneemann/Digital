/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

/**
 * An Map accessible from the template engine
 */
public interface HGSMap {

    /**
     * Sets a value to the map.
     *
     * @param key the key
     * @param val value
     * @throws HGSEvalException HGSEvalException
     */
    default void hgsMapPut(String key, Object val) throws HGSEvalException {
        throw new HGSEvalException("It's not allowed to set a value to this map!");
    }

    /**
     * Gets a value from the map.
     * This function returns null, if key is not present.
     *
     * @param key the key
     * @return the value, maybe null if not present
     * @throws HGSEvalException HGSEvalException
     */
    Object hgsMapGet(String key) throws HGSEvalException;

}


