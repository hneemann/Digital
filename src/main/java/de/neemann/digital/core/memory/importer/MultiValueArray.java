/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * Used to load a file to multiple RAM/ROM components
 */
public final class MultiValueArray implements ValueArray {
    private final ValueArray[] valueArrays;
    private final int bytes;
    private final int memCount;

    private MultiValueArray(ValueArray... valueArrays) {
        this.valueArrays = valueArrays;
        memCount = valueArrays.length;
        bytes = valueArrays[0].getBytesPerValue();
    }

    @Override
    public void set(int index, long value) {
        int a = index / memCount;
        int b = index % memCount;
        valueArrays[b].set(a, value);
    }

    @Override
    public long get(int index) {
        int a = index / memCount;
        int b = index % memCount;
        return valueArrays[b].get(a);
    }

    @Override
    public int getBytesPerValue() {
        return bytes;
    }

    /**
     * Builder to build the {@link MultiValueArray}
     */
    public static class Builder {
        private ArrayList<ValueArray> valueArrays;
        private int bytes;

        /**
         * Creates a new builder
         */
        public Builder() {
            valueArrays = new ArrayList<>();
        }

        /**
         * Adds a data field to the builder
         *
         * @param dataField the data field
         * @param bits      the bits used in the data field
         * @return this for chained calls
         * @throws ValueArrayException ValueArrayException
         */
        public Builder add(DataField dataField, int bits) throws ValueArrayException {
            return add(new DataFieldValueArray(dataField, bits));
        }

        /**
         * Adds a value array to the builder
         *
         * @param valueArray the value array to add
         * @return this for chained calls
         * @throws ValueArrayException ValueArrayException
         */
        public Builder add(ValueArray valueArray) throws ValueArrayException {
            if (valueArrays.isEmpty())
                bytes = valueArray.getBytesPerValue();

            if (valueArray.getBytesPerValue() != bytes)
                throw new ValueArrayException(Lang.get("err_allMemoriesNeedToHaveTheSameByteWidth"));

            valueArrays.add(valueArray);
            return this;
        }

        /**
         * Builds the {@link MultiValueArray}
         *
         * @return the {@link ValueArray}
         * @throws ValueArrayException ValueArrayException
         */
        public ValueArray build() throws ValueArrayException {
            switch (valueArrays.size()) {
                case 0:
                    throw new ValueArrayException(Lang.get("err_noRomFound"));
                case 1:
                    return valueArrays.get(0);
                default:
                    return new MultiValueArray(valueArrays.toArray(new ValueArray[0]));
            }
        }
    }

    /**
     * Error building the {@link MultiValueArray}
     */
    public static class ValueArrayException extends Exception {
        /**
         * Creates a new instance
         *
         * @param message the message
         */
        public ValueArrayException(String message) {
            super(message);
        }
    }
}
