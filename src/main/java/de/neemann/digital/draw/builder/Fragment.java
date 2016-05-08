package de.neemann.digital.draw.builder;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;

/**
 * @author hneemann
 */
public interface Fragment {

    Vector output();

    Box doLayout();

    void setPos(Vector pos);

    void addToCircuit(Vector pos, Circuit circuit);
}
