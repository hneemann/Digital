/*
 * Copyright (c) 2023 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.library.ElementTypeDescriptionCustom;
import de.neemann.digital.draw.library.GenericCode;
import de.neemann.digital.draw.library.GenericInitCode;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseElement;

import java.util.HashMap;
import java.util.HashSet;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * A shape created from the circuit inside.
 */
public class MinimizedShape implements Shape {
    private static final HashSet<String> IGNORE_SET = new HashSet<>();

    static {
        IGNORE_SET.add(In.DESCRIPTION.getName());
        IGNORE_SET.add(Out.DESCRIPTION.getName());
        IGNORE_SET.add(Clock.DESCRIPTION.getName());
        IGNORE_SET.add(GenericCode.DESCRIPTION.getName());
        IGNORE_SET.add(GenericInitCode.DESCRIPTION.getName());
        IGNORE_SET.add(TestCaseElement.DESCRIPTION.getName());
    }

    private final Pins pins;
    private final Circuit circuit;
    private final Vector min;
    private final Polygon outer;
    private final String name;
    private final Vector textPos;
    private final Vector labelPos;
    private final String label;


    /**
     * Creates a new instance.
     *
     * @param custom            then included circuit
     * @param elementAttributes the circuits attributes
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     * @throws PinException             PinException
     */
    public MinimizedShape(ElementTypeDescriptionCustom custom, ElementAttributes elementAttributes) throws NodeException, ElementNotFoundException, PinException {
        name = custom.getShortName();
        label = elementAttributes.getLabel();

        circuit = custom.getResolvedCircuit(elementAttributes);

        HashMap<String, Vector> pinMap = new HashMap<>();
        for (VisualElement ve : circuit.getElements()) {
            if (ve.equalsDescription(In.DESCRIPTION) || ve.equalsDescription(Clock.DESCRIPTION)) {
                addToMap(pinMap, ve);
            }
            if (ve.equalsDescription(Out.DESCRIPTION)) {
                addToMap(pinMap, ve);
            }
        }
        boolean first = true;
        int minX = 0;
        int minY = 0;
        for (Vector p : pinMap.values()) {
            if (first) {
                minX = p.x;
                minY = p.y;
                first = false;
            } else {
                if (p.x < minX) {
                    minX = p.x;
                }
                if (p.y < minY) {
                    minY = p.y;
                }
            }
        }
        min = new Vector(minX, minY);

        pins = new Pins();
        for (PinDescription p : custom.getInputDescription(elementAttributes))
            pins.add(new Pin(toGrid(pinMap.get(p.getName())), p));
        for (PinDescription p : custom.getOutputDescriptions(elementAttributes))
            pins.add(new Pin(toGrid(pinMap.get(p.getName())), p));

        GraphicMinMax minMax = new GraphicMinMax();
        drawCircuitTo(minMax);
        for (Pin p : pins)
            minMax.check(p.getPos().mul(2).add(min));

        outer = new Polygon(true)
                .add(new Vector(minMax.getMin().x, minMax.getMin().y - SIZE2))
                .add(new Vector(minMax.getMin().x, minMax.getMax().y + SIZE2))
                .add(new Vector(minMax.getMax().x, minMax.getMax().y + SIZE2))
                .add(new Vector(minMax.getMax().x, minMax.getMin().y - SIZE2));

        final int x = (minMax.getMin().x + minMax.getMax().x) / 2;
        labelPos = new Vector(x, minMax.getMin().y - SIZE2).sub(min).div(2).add(0, -SIZE2 / 2);
        textPos = new Vector(x, minMax.getMax().y + SIZE2).sub(min).div(2).add(0, SIZE2 / 2);
    }

    private void addToMap(HashMap<String, Vector> pinMap, VisualElement ve) throws PinException {
        final String label = ve.getElementAttributes().getLabel();
        if (pinMap.containsKey(label))
            throw new PinException(Lang.get("err_duplicatePinLabel", label, name));
        pinMap.put(label, ve.getPos());
    }

    private Vector toGrid(VectorInterface pos) {
        pos = pos.sub(min).div(2);
        return new Vector(Math.round(pos.getXFloat() / SIZE) * SIZE, Math.round(pos.getYFloat() / SIZE) * SIZE);
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        Graphic gr = new GraphicTransform(graphic,
                Transform.mul(
                        new TransformTranslate(min.mul(-1)),
                        TransformMatrix.scale(0.5f, 0.5f)));

        gr = new GraphicRestyle(gr) {
            @Override
            public Style getStyle(Style style) {
                if (style != Style.DASH)
                    return Style.THIN;
                else
                    return style;
            }

            @Override
            public Style getFontStyle(Style style) {
                return Style.THIN.deriveFontStyle(style.getFontSize() / 2, false);
            }
        };

        drawCircuitTo(gr);
        gr.drawPolygon(outer, Style.DASH);

        if (label != null && label.length() > 0)
            graphic.drawText(labelPos, label, Orientation.CENTERBOTTOM, Style.NORMAL);

        if (name.length() > 0)
            graphic.drawText(textPos, name, Orientation.CENTERTOP, Style.SHAPE_PIN);
    }

    private void drawCircuitTo(Graphic gr) {
        for (Wire w : circuit.getWires())
            gr.drawLine(w.p1, w.p2, Style.THIN);
        for (VisualElement ve : circuit.getElements())
            if (!IGNORE_SET.contains(ve.getElementName()))
                ve.drawTo(gr, null);
    }

    @Override
    public Pins getPins() {
        return pins;
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        return null;
    }
}
