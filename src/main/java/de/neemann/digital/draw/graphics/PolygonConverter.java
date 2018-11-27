/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter to write a polygon to a xml file
 */
public class PolygonConverter implements Converter {

    @Override
    public boolean canConvert(Class aClass) {
        return aClass.equals(Polygon.class);
    }

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {
        Polygon p = (Polygon) o;
        writer.addAttribute("path", p.toString());
        writer.addAttribute("evenOdd", Boolean.toString(p.getEvenOdd()));
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext unmarshallingContext) {
        String path = reader.getAttribute("path");
        boolean evenOdd = Boolean.parseBoolean(reader.getAttribute("evenOdd"));
        final Polygon polygon = Polygon.createFromPath(path);
        if (polygon != null)
            polygon.setEvenOdd(evenOdd);
        return polygon;
    }

}
