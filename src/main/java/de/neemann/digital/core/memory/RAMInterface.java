/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import de.neemann.digital.core.IntFormat;
import de.neemann.digital.core.stats.Countable;

/**
 * Interface to get access to the rams data.
 */
public interface RAMInterface extends ProgramMemory, Countable {
    /**
     * @return the {@link DataField} containing the RAMs data
     */
    DataField getMemory();

    /**
     * @return the rams size
     */
    int getSize();

    /**
     * @return the addr bits
     */
    int getAddrBits();

    /**
     * @return the integer format to be used to visualize the values
     */
    default IntFormat getIntFormat() {
        return IntFormat.hex;
    }
}
