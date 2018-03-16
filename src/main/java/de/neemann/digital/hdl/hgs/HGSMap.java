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
     * Returns true if the map contains the key.
     * If this method always returns true, it's not possible to write new
     * values in the map. In this case you can only overwrite existing values.
     *
     * @param key the key
     * @return true if contained in the map
     * @throws HGSEvalException HGSEvalException
     */
    default boolean hgsMapContains(String key) throws HGSEvalException {
        return true;
    }

    /**
     * Sets a value to the map.
     *
     * @param key the key
     * @param val value
     * @throws HGSEvalException HGSEvalException
     */
    void hgsMapPut(String key, Object val) throws HGSEvalException;

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


