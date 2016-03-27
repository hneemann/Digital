package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.RAMInterface;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.DataEditor;

import java.awt.*;

/**
 * @author hneemann
 */
public class RAMShape extends GenericShape {
    public RAMShape(String name, String[] inputs, OutputPinInfo[] outputs, String label) {
        super(name, inputs, outputs, label, true);
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState, Observer guiObserver) {
        return new Interactor() {
            @Override
            public void clicked(CircuitComponent cc, Point pos, IOState ioState, Element element) {
                if (element instanceof RAMInterface) {
                    DataField dataField = ((RAMInterface) element).getMemory();
                    new DataEditor(cc, dataField).showDialog();
                }
            }
        };
    }
}
