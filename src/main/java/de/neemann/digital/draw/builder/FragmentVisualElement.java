package de.neemann.digital.draw.builder;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.shapes.ShapeFactory;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class FragmentVisualElement implements Fragment {

    private final ArrayList<Pin> inputs;
    private final ArrayList<Pin> outputs;
    private final VisualElement visualElement;
    private Vector pos;

    public FragmentVisualElement(ElementTypeDescription description, int inputCount, ShapeFactory shapeFactory) {
        visualElement = new VisualElement(description.getName()).setShapeFactory(shapeFactory);
        visualElement.getElementAttributes().set(Keys.INPUT_COUNT, inputCount);
        Pins pins = visualElement.getShape().getPins();

        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        for (Pin p : pins) {
            if (p.getDirection().equals(PinDescription.Direction.input))
                inputs.add(p);
            else
                outputs.add(p);
        }
    }

    @Override
    public Vector output() {
        return outputs.get(0).getPos();
    }

    @Override
    public Box doLayout() {
        GraphicMinMax mm = new GraphicMinMax();
        for (Pin p : inputs)
            mm.check(p.getPos());
        for (Pin p : outputs)
            mm.check(p.getPos());
        Vector delta = mm.getMax().sub(mm.getMin());
        return new Box(delta.x, delta.y);
    }

    @Override
    public void setPos(Vector pos) {
        this.pos = pos;
    }

    @Override
    public void addToCircuit(Vector offset, Circuit circuit) {
        System.out.println(visualElement.getElementName() + ", " + pos + ", " + offset);
        visualElement.setPos(pos.add(offset));
        circuit.add(visualElement);
    }
}
