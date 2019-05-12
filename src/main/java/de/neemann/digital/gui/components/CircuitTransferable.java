/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Movable;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.shapes.ShapeFactory;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Used to implement copy and paste.
 */
public class CircuitTransferable implements Transferable {

    private String data;

    /**
     * Creates a new instance
     *
     * @param data the data to copy
     */
    CircuitTransferable(ArrayList<Movable> data) {
        XStream xStream = Circuit.getxStream();
        try (StringWriter out = new StringWriter()) {
            out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            xStream.marshal(data, new PrettyPrintWriter(out));
            this.data = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.stringFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == DataFlavor.stringFlavor;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor))
            throw new UnsupportedFlavorException(flavor);

        return data;
    }

    /**
     * Creates a list of objects from inserted data
     * Can also throw a {@link ClassCastException}!
     *
     * @param data         the inserted data
     * @param shapeFactory the shapeFactory to set to the elements
     * @return the elements or null
     * @throws IOException IOException
     */
    public static ArrayList<Movable> createList(Object data, ShapeFactory shapeFactory) throws IOException {
        if (!(data instanceof String))
            return null;

        XStream xStream = Circuit.getxStream();
        try (Reader in = new StringReader(data.toString())) {
            ArrayList<Movable> elements = (ArrayList<Movable>) xStream.fromXML(in);
            if (elements == null)
                return null;

            for (Movable m : elements)
                if (m instanceof VisualElement)
                    ((VisualElement) m).setShapeFactory(shapeFactory);

            return elements;
        }
    }
}
