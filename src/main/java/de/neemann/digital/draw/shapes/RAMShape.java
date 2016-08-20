package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
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
 *
 * @author hneemann
 */
public class RAMShape extends GenericShape {
    private final int bits;
    private final int size;

    /**
     * Creates a new instance
     *
     * @param attr    the label to use
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public RAMShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        super("RAM", inputs, outputs, attr.getLabel(), true);
        bits = attr.get(Keys.BITS);
        size = 1 << attr.get(Keys.ADDR_BITS);
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return new Interactor() {
            @Override
            public boolean clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync) {
                if (element instanceof RAMInterface) {
                    DataField dataField = ((RAMInterface) element).getMemory();
                    new DataEditor(cc, dataField, size, bits, true, modelSync).showDialog();
                }
                return false;
            }
        };
    }
}
