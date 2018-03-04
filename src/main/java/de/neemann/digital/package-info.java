/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */

/**
 * Digital is build from several packages:
 * <p>
 * The package gui contains all the GUI classes. The class {@link de.neemann.digital.gui.Main} contains the
 * main method and the generation of the main frame. One of the more important class is
 * {@link de.neemann.digital.gui.components.CircuitComponent}. This class is used to draw the circuit.
 * <p>
 * There are two representations of the digital circuit. One is build up with
 * {@link de.neemann.digital.draw.elements.VisualElement} instances representing the elements in the circuit
 * and {@link de.neemann.digital.draw.elements.Wire} which simply represents a wire. The class
 * {@link de.neemann.digital.draw.elements.Circuit} contains a set of both elements.
 * This representation is also stored on disk. You can find the necessary classes in the package draw.
 * Below I call this representation the circuit.
 * <p>
 * The other representation of the digital circuit is build by the {@link de.neemann.digital.core.Node}
 * classes which you can find in the core package.
 * This classes form the simulation model represented by the {@link de.neemann.digital.core.Model} class.
 * Furthermore this representation is called the model.
 * In the package draw.model you can find the class {@link de.neemann.digital.draw.model.ModelCreator}.
 * This class takes a circuit represented by a Circuit instance and creates
 * a Model instance representation.
 * Some of the elements out of the Circuit representation you can also find
 * in the model, some you don't. So all the wires don't exist anymore in the
 * model. They are resolved and replaced by a simple Observable-Observer pattern.
 * Also all the inputs and outputs of the circuit and nested circuits don't exist in the
 * model. They are also resolved and replaced by the Observable-Observer pattern.
 * <p>
 * The graphical representation (Circuit) is able to hold references to the
 * model representation (Model). So the graphical representation is able to reflect the
 * models actual state. But this is not necessary: The model can also be used without the
 * graphical representation which it is derived from.
 * <p>
 * The model class also has the ability to run the model. Sometime there maybe will be
 * an other simulation algorithm. Up to now there is only a very simple algorithm available.
 * <p>
 * The model consists only of {@link de.neemann.digital.core.Node}s. Such a nodes contain one or more
 * {@link de.neemann.digital.core.ObservableValue} instances which represent the inputs and outputs of the node.
 * To the ObservableValue you can register a {@link de.neemann.digital.core.Observer} instances. Every node
 * implements the Observer interface. So every node can be registered to a ObservableValue an so it is notified
 * on a state change of the ObservableValue.
 * <p>
 * An example: An AND gate with two inputs is registered to the two
 * ObservableValues representing their inputs. So, if one of the input values changes, the AND gate gets a
 * notification. But the AND gate does not directly react on this notification. It only informs the
 * model that there was a change at one of the inputs. If the model has collected all the
 * nodes which had seen an input change, it asks all this nodes
 * to update their outputs. So it looks like all the gates react at the same time on an input change.
 * <p>
 * If the nodes update their outputs this causes new nodes to inform the model
 * about changes and so on and so on. Every such iteration is called a micro step.
 * If this iteration stops because no new nodes have seen an input change, the model has stabilized again.
 * Now the model can handle a new clock change or a new user interaction.
 */
package de.neemann.digital;
