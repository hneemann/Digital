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
}
