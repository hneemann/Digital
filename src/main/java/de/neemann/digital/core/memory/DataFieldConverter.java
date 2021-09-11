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

import java.util.StringTokenizer;

/**
 * Optimized converter for data fields
 */
public class DataFieldConverter implements Converter {
    @Override
    public boolean canConvert(Class aClass) {
        return aClass.equals(DataField.class);
    }

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {
        DataField df = (DataField) o;
        df.trim();
        StringBuilder dataStr = new StringBuilder();
        long[] data = df.getData();
        int pos = 0;
        if (data.length > 0) {
            long akt = data[0];
            int count = 1;
            for (int i = 1; i < data.length; i++) {
                if (dataStr.length() - pos > 60) {
                    dataStr.append("\n");
                    pos = dataStr.length();
                }

                final long now = data[i];
                if (now == akt)
                    count++;
                else {
                    writeChunk(dataStr, akt, count);
                    akt = now;
                    count = 1;
                }
            }
            writeChunk(dataStr, akt, count);
        }
        writer.setValue(dataStr.toString());
    }

    private void writeChunk(StringBuilder w, long data, int count) {
        if (count < 4) {
            for (int j = 0; j < count; j++) {
                if (w.length() > 0)
                    w.append(",");
                w.append(Long.toHexString(data));
            }
        } else {
            if (w.length() > 0)
                w.append(",");
            w.append(count);
            w.append('*');
            w.append(Long.toHexString(data));
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext unmarshallingContext) {
        if (reader.hasMoreChildren()) {
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
                DataField df = new DataField(1024);
                StringTokenizer st = new StringTokenizer(reader.getValue(), ",");
                int i = 0;
                while (st.hasMoreTokens()) {
                    String val = st.nextToken().trim();
                    int p = val.indexOf("*");
                    if (p < 0) {
                        df.setData(i, Bits.decode(val, 0, 16));
                        i++;
                    } else {
                        int count = Integer.parseInt(val.substring(0, p));
                        long v = Bits.decode(val.substring(p + 1), 0, 16);
                        for (int j = 0; j < count; j++) {
                            df.setData(i, v);
                            i++;
                        }
                    }
                }
                df.trim();
                return df;
            } catch (Bits.NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
