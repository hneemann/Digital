package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.RAMInterface;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.DataEditor;
import de.neemann.digital.gui.sync.Sync;

import java.awt.*;

/**
 * The RAM shape
 * @author hneemann
 */
public class RAMShape extends GenericShape {
    /**
     * Creates a new instance
     *
     * @param name    name of the element
     * @param inputs  the inputs
     * @param outputs the outputs
     * @param label   the label to use
     */
    public RAMShape(String name, PinDescriptions inputs, PinDescriptions outputs, String label) {
        super(name, inputs, outputs, label, true);
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return new Interactor() {
            @Override
            public boolean clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync) {
                if (element instanceof RAMInterface) {
                    DataField dataField = ((RAMInterface) element).getMemory();
                    new DataEditor(cc, dataField, modelSync).showDialog();
                }
                return false;
            }
        };
    }
}
