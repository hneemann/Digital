package de.neemann.digital.draw.builder;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;

import java.util.List;

/**
 * @author hneemann
 */
public interface Fragment {

    Box doLayout();

    void setPos(Vector pos);

    void addToCircuit(Vector pos, Circuit circuit);

    List<Vector> getInputs();

    List<Vector> getOutputs();

}
