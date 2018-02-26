package de.neemann.digital.core.memory.rom;

import de.neemann.digital.core.memory.DataField;

/**
 * Interface implemented by al ROM or EEPROM components
 */
public interface ROMInterface {
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

}
