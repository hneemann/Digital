/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Model;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.model.ModelEntry;

/**
 * Every element you can put on the circuit panel implements this interface.
 */
public interface Shape extends Drawable, ObservableValueReader {

    /**
     * Puts the pins name and the pins x-y-position together!
     * This information is used to calculate the models connections from the wiring in the circuit.
     * Don't create your own {@link de.neemann.digital.core.element.PinInfo} instance! Try to use
     * the instances passed to the constructor of the shape via the {@link ShapeFactory}s {@link ShapeFactory.Creator} interface.
     *
     * @return the pins
     */
    Pins getPins();

    /**
     * This method call connects the created model element to the shape which represents the model node.
     * To access the actual state while drawing, the Shape needs to store the IOState or the needed inputs
     * in a member variable.
     * If the shape returns an interactor, this interactors clicked method is called if the
     * shape is clicked in running mode. If the shape is not interactive simply return null.
     *
     * @param ioState the state of the element, never null
     * @return The interactor which is used to interact with the shape while the simulation runs.
     */
    InteractorInterface applyStateMonitor(IOState ioState);

    /**
     * Allows the shape to make its drawing dependent of the model by registering a Observer to the model.
     * It is used by {@link DataShape} to create and show the data graph.
     * This method is necessary if a shape does not depend only on its inputs or outputs but if it
     * depends on the global model state.
     *
     * @param modelCreator the models description
     * @param model        the model itself
     * @param element      the ModelElement this shape belongs to
     */
    default void registerModel(ModelCreator modelCreator, Model model, ModelEntry element) {
    }

}
