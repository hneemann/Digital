/**
 * Digital is build from several packages:
 * <p>
 * The package gui contains all the GUI classes. The class {@link de.neemann.digital.gui.Main} contains the
 * main method ald the generation of the main frame. One of the more important class is
 * {@link de.neemann.digital.gui.components.CircuitComponent}. This class is used to draw the circuit.
 * <p>
 * There are two representations of the circuit. One is build up with
 * {@link de.neemann.digital.draw.elements.VisualElement} instances representing the elements in the circuit
 * and {@link de.neemann.digital.draw.elements.Wire} which simply represents a wire. The class
 * {@link de.neemann.digital.draw.elements.Circuit} contains both elements.
 * This representation is also stored on disk. You can find the necessary classes in the package draw.
 * <p>
 * The other representation of the circuit is build by the {@link de.neemann.digital.core.Node}
 * classes which you can find in the code package.
 * This classes form the simulation model represented by the {@link de.neemann.digital.core.Model} class.
 * In the package draw.model you can find the class {@link de.neemann.digital.draw.model.ModelDescription}.
 * This class takes a circuit represented by a Circuit instance and creates
 * a Model instance representation.
 * Some of the elements from the Circuit representation you can also find
 * in the Model some you don't. So all the wires don't exist anymore in the
 * Model. They are resolved and replaced by a simple Observable-Observer system.
 * Also all the inputs and outputs of the circuit and nested circuits don't exist in the
 * Model. They are also resolved and replaced by the Observable-Observer system.
 * <p>
 * The graphical representation (Circuit) is able to hold references to the
 * Model representation (Model). So the graphical representation is able to reflect the
 * models actual state. But this is not necessary: The model can also be used without the graphical representation which
 * it is derived from.
 * <p>
 * The Model class also has the ability to run the model. Sometime there maybe will be
 * an other simulation algorithm. Up to now there is only a very simple algorithm available.
 * <p>
 * The Model consists only of {@link de.neemann.digital.core.Node}s. This Nodes contain
 * {@link de.neemann.digital.core.ObservableValue} instances which represent the inputs and outputs of the nodes.
 * To the ObservableValue you can register a {@link de.neemann.digital.core.Observer} instances. Every Node implements the
 * Observer interface. So every Node can be notified on ObservableValue changes.
 * <p>
 * An Example: An AND gate with two inputs is registered to two
 * ObservableValues. So, if one of the input values changes, the AND gate gets a
 * notification. But the AND gate does not directly react on this notification. It only informs the
 * Model that there was a change at one of the inputs. If the model has collected all the
 * Nodes which had seen an input change, it asks all this Nodes
 * to update their outputs. This causes new Nodes to inform the Model
 * about changes and so on and so on. Every such iteration is called a micro step.
 * If this iteration stops, the Model has stabilized again. Now the Model can handle a new
 * clock change or a new user interaction.
 *
 * @author hneemann
 */
package de.neemann.digital;
