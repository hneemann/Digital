/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import de.neemann.digital.core.memory.DataField;

/**
 * Maps a dataField to a value array
 */
public class DataFieldValueArray implements ValueArray {

    private final DataField dataField;
    private final int bytes;

    /**
     * Creates a new instance
     *
     * @param dataField the data field to used
     * @param bits      the number of bits used in the data field
     */
    public DataFieldValueArray(DataField dataField, int bits) {
        this.dataField = dataField;
        this.bytes = (bits - 1) / 8 + 1;
    }

    @Override
    public void set(int index, long value) {
        dataField.setData(index, value);
    }

    @Override
    public long get(int index) {
        return dataField.getDataWord(index);
    }

    @Override
    public int getBytesPerValue() {
        return bytes;
    }
}
