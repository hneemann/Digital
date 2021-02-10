/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;

/**
 * Converter for truth tables.
 * The created output is much more readable.
 */
public class TruthTableConverter implements Converter {

    @Override
    public boolean canConvert(Class aClass) {
        return aClass.equals(BoolTableByteArray.class);
    }

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {
        BoolTableByteArray bt = (BoolTableByteArray) o;
        writer.setValue(bt.toString());
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext unmarshallingContext) {
        if (reader.hasMoreChildren()) {
            // is old format, read base64 encoded byte array
            reader.moveDown();
            Object o = unmarshallingContext.convertAnother(new byte[]{}, byte[].class);
            reader.moveUp();
            return new BoolTableByteArray((byte[]) o);
        } else {
            String values = reader.getValue();
            return new BoolTableByteArray(values);
        }
    }

}
