package de.neemann.digital.gui.components;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Moveable;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.Vector;
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
 * @author hneemann
 */
public class CircuitTransferable implements Transferable {

    private String data;

    /**
     * Creates a new instance
     *
     * @param data the data to copy
     */
    CircuitTransferable(ArrayList<Moveable> data) {
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
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
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
     * @param lastMousePos the actual mouse position
     * @return the elements or null
     * @throws IOException IOException
     */
    static ArrayList<Moveable> createList(Object data, ShapeFactory shapeFactory, Vector lastMousePos) throws IOException {
        if (!(data instanceof String))
            return null;

        XStream xStream = Circuit.getxStream();
        Vector max = null;
        try (Reader in = new StringReader(data.toString())) {
            ArrayList<Moveable> elements = (ArrayList<Moveable>) xStream.fromXML(in);
            for (Moveable m : elements)
                if (m instanceof VisualElement) {
                    ((VisualElement) m).setShapeFactory(shapeFactory);
                    GraphicMinMax mm = ((VisualElement) m).getMinMax(false);
                    if (max == null)
                        max = mm.getMax();
                    else
                        max = Vector.max(max, mm.getMax());
                }

            if (max != null) {
                Vector delta = CircuitComponent.raster(lastMousePos.sub(max));
                for (Moveable m : elements)
                    m.move(delta);
            }

            return elements;
        }
    }
}
