/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import de.neemann.digital.core.Bits;

import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * Optimized converter for data fields
 * <p>
 */
public class DataFieldConverter implements Converter {
    @Override
    public boolean canConvert(Class aClass) {
        return aClass.equals(DataField.class);
    }

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {
        DataField df = (DataField) o;
        df = df.getMinimized();
        //writer.startNode("data");
        writer.addAttribute("size", Integer.toString(df.size()));
        StringBuilder data = new StringBuilder();
        int pos = 0;
        for (long d : df.getData()) {
            if (data.length() > 0) {
                data.append(",");
                pos++;
            }

            if (pos > 80) {
                data.append("\n");
                pos = 0;
            }

            final String s = Long.toHexString(d);
            data.append(s);
            pos += s.length();
        }
        writer.setValue(data.toString());
        //writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext unmarshallingContext) {
        if (reader.getAttribute("size") == null) {
            // old type
            reader.moveDown();
            DataField df = new DataField(Integer.parseInt(reader.getValue()));
            reader.moveUp();
            int i = 0;
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                df.setData(i, Long.parseLong(reader.getValue()));
                i++;
                reader.moveUp();
            }
            return df;
        } else {
            try {
                // new type
                int size = Integer.parseInt(reader.getAttribute("size"));
                long[] data = new long[size];
                StringTokenizer st = new StringTokenizer(reader.getValue(), ",");
                int i = 0;
                while (st.hasMoreTokens()) {
                    data[i] = Bits.decode(st.nextToken().trim(), 0, 16);
                    i++;
                }
                return new DataField(Arrays.copyOf(data, i), size);
            } catch (Bits.NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
