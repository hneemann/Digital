/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.rom;

import de.neemann.digital.core.IntFormat;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.stats.Countable;

/**
 * Interface implemented by al ROM or EEPROM components
 */
public interface ROMInterface extends Countable {
    /**
     * Sets the data for this ROM element
     *
     * @param data data to use
     */
    void setData(DataField data);

    /**
     * @return the ROM's label
     */
    String getLabel();

    /**
     * @return number of data bits
     */
    int getDataBits();

    /**
     * @return number of address bits
     */
    int getAddrBits();

    /**
     * @return the integer format to be used to visualize the values
     */
    IntFormat getIntFormat();
}
