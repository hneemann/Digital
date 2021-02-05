/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.library.ElementTypeDescriptionCustom;
import de.neemann.digital.lang.Lang;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * The layout shape.
 * The pins are ordered in the same way they are placed inside the circuit.
 * Thus the shape feels like a minimized version of the contained circuit in respect to pin ordering.
 */
public class LayoutShape implements Shape {

    private final int width;
    private final int height;
    private final Pins pins;
    private final Color color;
    private final String name;
    private final PinList left;
    private final PinList right;
    private final PinList top;
    private final PinList bottom;

    /**
     * Creates a new instance
     *
     * @param custom            the type description
     * @param elementAttributes the local attributes
     * @throws NodeException            NodeException
     * @throws PinException             PinException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public LayoutShape(ElementTypeDescriptionCustom custom, ElementAttributes elementAttributes) throws NodeException, PinException, ElementNotFoundException {
        String l = elementAttributes.getLabel();
        if (l != null && l.length() > 0)
            name = l;
        else
            name = custom.getShortName();

        left = new PinList(name, false);
        right = new PinList(name, false);
        top = new PinList(name, true);
        bottom = new PinList(name, true);

        Circuit circuit = custom.getResolvedCircuit(elementAttributes);

        for (VisualElement ve : circuit.getElements()) {
            if (ve.equalsDescription(In.DESCRIPTION) || ve.equalsDescription(Clock.DESCRIPTION)) {
                switch (ve.getRotate()) {
                    case 0:
                        left.add(ve);
                        break;
                    case 1:
                        bottom.add(ve);
                        break;
                    case 2:
                        right.add(ve);
                        break;
                    default:
                        top.add(ve);
                        break;
                }
            }
            if (ve.equalsDescription(Out.DESCRIPTION)) {
                switch (ve.getRotate()) {
                    case 0:
                        right.add(ve);
                        break;
                    case 1:
                        top.add(ve);
                        break;
                    case 2:
                        left.add(ve);
                        break;
                    default:
                        bottom.add(ve);
                        break;
                }
            }
        }

        height = left.max(right.max(circuit.getAttributes().get(Keys.HEIGHT)));
        width = top.max(bottom.max(circuit.getAttributes().get(Keys.WIDTH)));

        HashMap<String, PinPos> map = new HashMap<>();
        top.createPosition(map, new Vector(0, 0), width);
        bottom.createPosition(map, new Vector(0, SIZE * height), width);
        left.createPosition(map, new Vector(0, 0), height);
        right.createPosition(map, new Vector(SIZE * width, 0), height);

        pins = new Pins();
        for (PinDescription p : custom.getInputDescription(elementAttributes))
            pins.add(createPin(map, p));
        for (PinDescription p : custom.getOutputDescriptions(elementAttributes))
            pins.add(createPin(map, p));

        color = circuit.getAttributes().get(Keys.BACKGROUND_COLOR);
    }

    private Pin createPin(HashMap<String, PinPos> map, PinDescription p) throws PinException {
        PinPos pinPos = map.get(p.getName());
        if (pinPos == null)
            throw new PinException(Lang.get("err_pin_N_notFound", p.getName()));
        return new Pin(pinPos.pos, p);
    }


    @Override
    public Pins getPins() {
        return pins;
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        final Polygon poly = new Polygon(true)
                .add(0, 0)
                .add(width * SIZE, 0)
                .add(width * SIZE, height * SIZE)
                .add(0, height * SIZE);
        graphic.drawPolygon(poly, Style.NORMAL.deriveFillStyle(color));
        graphic.drawPolygon(poly, Style.NORMAL);

        if (bottom.size() == 0)
            graphic.drawText(new Vector(width * SIZE / 2, height * SIZE + 4), name, Orientation.CENTERTOP, Style.SHAPE_PIN);
        else if (top.size() == 0)
            graphic.drawText(new Vector(width * SIZE / 2, -4), name, Orientation.CENTERBOTTOM, Style.SHAPE_PIN);
        else
            graphic.drawText(new Vector(width * SIZE / 2, height * SIZE / 2), name, Orientation.CENTERCENTER, Style.SHAPE_PIN);

        for (PinPos p : left)
            graphic.drawText(p.pos.add(4, 0), p.label, Orientation.LEFTCENTER, Style.SHAPE_PIN);
        for (PinPos p : right)
            graphic.drawText(p.pos.add(-4, 0), p.label, Orientation.RIGHTCENTER, Style.SHAPE_PIN);
        for (PinPos p : top)
            graphic.drawText(p.pos.add(0, 4), p.pos.add(0, 3), p.label, Orientation.RIGHTCENTER, Style.SHAPE_PIN);
        for (PinPos p : bottom)
            graphic.drawText(p.pos.add(0, -4), p.pos.add(0, -3), p.label, Orientation.RIGHTCENTER, Style.SHAPE_PIN);
    }

    private final static class PinPos implements Comparable<PinPos> {
        private final int orderPos;
        private final String label;
        private boolean hasPosDelta;
        private int posDelta;
        private Vector pos;

        private PinPos(VisualElement ve, boolean horizontal) {
            if (horizontal)
                orderPos = ve.getPos().x;
            else
                orderPos = ve.getPos().y;
            label = ve.getElementAttributes().getLabel();

            posDelta = ve.getElementAttributes().get(Keys.LAYOUT_SHAPE_DELTA);
            hasPosDelta = posDelta > 0;
        }

        @Override
        public int compareTo(PinPos pinPos) {
            return orderPos - pinPos.orderPos;
        }

    }

    private final static class PinList implements Iterable<PinPos> {
        private final String name;
        private final boolean horizontal;
        private ArrayList<PinPos> pins;
        private boolean allHavePosDeltas = false;
        private Vector pos;
        private int minWidth;

        private PinList(String name, boolean horizontal) {
            this.name = name;
            this.horizontal = horizontal;
            pins = new ArrayList<>();
        }

        private void add(VisualElement ve) {
            PinPos pp = new PinPos(ve, horizontal);
            pins.add(pp);
            if (pp.hasPosDelta)
                minWidth += pp.posDelta;
            else
                allHavePosDeltas = false;
        }

        private int size() {
            return pins.size();
        }

        private void createPosition(HashMap<String, PinPos> map, Vector startPos, int length) throws PinException {
            this.pos = startPos;
            Collections.sort(pins);

            if (allHavePosDeltas) {
                for (PinPos pp : pins) {
                    move(pp.posDelta);
                    pp.pos = pos;
                    addToMap(map, pp);
                }
            } else {
                // length: user defined width, always larger or equal to pins.size()+1

                int delta = (length + 2) / (pins.size() + 1);

                int pinsOnly = delta * (pins.size() - 1);

                move((length - pinsOnly) / 2);

                for (PinPos pp : pins) {
                    pp.pos = pos;
                    addToMap(map, pp);
                    move(delta);
                }
            }
        }

        private void addToMap(HashMap<String, PinPos> map, PinPos pp) throws PinException {
            if (map.containsKey(pp.label))
                throw new PinException(Lang.get("err_duplicatePinLabel", pp.label, name));
            map.put(pp.label, pp);
        }

        private void move(int delta) {
            if (horizontal)
                pos = pos.add(SIZE * delta, 0);
            else
                pos = pos.add(0, SIZE * delta);
        }

        private int max(int m) {
            if (allHavePosDeltas)
                m = Math.max(m, minWidth + 1);
            return Math.max(m, pins.size() + 1);
        }

        @Override
        public Iterator<PinPos> iterator() {
            return pins.iterator();
        }
    }
}
