package de.neemann.digital.core.memory;

/**
 * Interface to get access to the rams data.
 *
 * @author hneemann
 */
public interface RAMInterface {
    /**
     * @return the {@link DataField} containing the RAMs data
     */
    DataField getMemory();

    /**
     * @return the name of the memory
     */
    String getLabel();

    /**
     * @return the rams size
     */
    int getSize();

    /**
     * @return the data bits
     */
    int getDataBits();

    /**
     * @return the addr bits
     */
    int getAddrBits();
}
