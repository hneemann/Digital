package de.neemann.digital.core.memory;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.StringTokenizer;

/**
 * Optimized converter for data fields
 * <p>
 * Created by hneemann on 14.12.16.
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

            final String s = Long.toString(d);
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
            DataField df = new DataField(Integer.valueOf(reader.getValue()));
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
            // new type
            int size = Integer.parseInt(reader.getAttribute("size"));
            DataField df = new DataField(size);
            StringTokenizer st = new StringTokenizer(reader.getValue(), ",");
            int i = 0;
            while (st.hasMoreTokens()) {
                df.setData(i, Long.parseLong(st.nextToken().trim()));
                i++;
            }
            return df;
        }
    }

}
