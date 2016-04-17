package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.model.ModelDescription;
import de.neemann.digital.draw.model.ModelEntry;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.OrderMerger;
import de.neemann.digital.gui.components.data.DataSet;
import de.neemann.digital.gui.components.data.DataSetObserver;

import java.awt.*;
import java.util.ArrayList;

/**
 * Shape which shows the data graph inside the models circuit area.
 *
 * @author hneemann
 */
public class DataShape implements Shape {

    private final boolean microStep;
    private final int maxSize;
    private DataSet dataSet;

    /**
     * Creates a new instance
     *
     * @param attr    the attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public DataShape(ElementAttributes attr, PinDescription[] inputs, PinDescription[] outputs) {
        microStep = attr.get(Keys.MICRO_STEP);
        maxSize = attr.get(Keys.MAX_STEP_COUNT);
    }

    @Override
    public Pins getPins() {
        return new Pins();
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return new Interactor() {
            @Override
            public boolean clicked(CircuitComponent cc, Point pos, IOState ioState, Element element) {
                dataSet.clear();
                return false;
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic, boolean heighLight) {
        if (dataSet == null) {
            dataSet = new DataSet();
        }
        dataSet.drawTo(graphic, false);
    }

    @Override
    public void registerModel(ModelDescription modelDescription, Model model, ModelEntry element) {
        ArrayList<Model.Signal> signals = model.getSignalsCopy();
        new OrderMerger<String, Model.Signal>(modelDescription.getCircuit().getMeasurementOrdering()) {
            @Override
            public boolean equals(Model.Signal a, String b) {
                return a.getName().equals(b);
            }
        }.order(signals);

        dataSet = new DataSet(signals, maxSize);

        DataSetObserver dataSetObserver = new DataSetObserver(microStep, dataSet);
        model.addObserver(dataSetObserver);
    }
}
