/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.library.ElementLibrary;
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

    private int width;
    private int height;
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
     * @throws NodeException NodeException
     * @throws PinException  PinException
     */
    public LayoutShape(ElementLibrary.ElementTypeDescriptionCustom custom, ElementAttributes elementAttributes) throws NodeException, PinException {
        left = new PinList(false);
        right = new PinList(false);
        top = new PinList(true);
        bottom = new PinList(true);

        for (VisualElement ve : custom.getCircuit().getElements()) {
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

        height = Math.max(Math.max(right.size(), left.size()) + 1, custom.getAttributes().get(Keys.HEIGHT));
        width = Math.max(Math.max(top.size(), bottom.size()) + 1, custom.getAttributes().get(Keys.WIDTH));

        HashMap<String, PinPos> map = new HashMap<>();
        top.createPosition(map, new Vector(((width - top.size() - 1) / 2 + 1) * SIZE, 0));
        bottom.createPosition(map, new Vector(((width - bottom.size() - 1) / 2 + 1) * SIZE, SIZE * height));
        left.createPosition(map, new Vector(0, ((height - left.size() - 1) / 2 + 1) * SIZE));
        right.createPosition(map, new Vector(SIZE * width, ((height - right.size() - 1) / 2 + 1) * SIZE));

        pins = new Pins();
        for (PinDescription p : custom.getInputDescription(elementAttributes))
            pins.add(createPin(map, p));
        for (PinDescription p : custom.getOutputDescriptions(elementAttributes))
            pins.add(createPin(map, p));

        color = custom.getCircuit().getAttributes().get(Keys.BACKGROUND_COLOR);

        String l = elementAttributes.getCleanLabel();
        if (l != null && l.length() > 0)
            name = l;
        else
            name = custom.getShortName();
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
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
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

        if (top.size() == 0)
            Graphic.drawText(graphic, new Vector(width * SIZE / 2, -4), name, Orientation.CENTERBOTTOM, Style.SHAPE_PIN);
        else if (bottom.size() == 0)
            Graphic.drawText(graphic, new Vector(width * SIZE / 2, height * SIZE + 4), name, Orientation.CENTERTOP, Style.SHAPE_PIN);
        else
            Graphic.drawText(graphic, new Vector(width * SIZE / 2, height * SIZE / 2), name, Orientation.CENTERCENTER, Style.SHAPE_PIN);

        for (PinPos p : left)
            Graphic.drawText(graphic, p.pos.add(4, 0), p.label, Orientation.LEFTCENTER, Style.SHAPE_PIN);
        for (PinPos p : right)
            Graphic.drawText(graphic, p.pos.add(-4, 0), p.label, Orientation.RIGHTCENTER, Style.SHAPE_PIN);
        for (PinPos p : top)
            graphic.drawText(p.pos.add(0, 4), p.pos.add(0, 3), p.label, Orientation.RIGHTCENTER, Style.SHAPE_PIN);
        for (PinPos p : bottom)
            graphic.drawText(p.pos.add(0, -4), p.pos.add(0, -3), p.label, Orientation.RIGHTCENTER, Style.SHAPE_PIN);
    }

    private final static class PinPos implements Comparable<PinPos> {
        private final int orderPos;
        private final String label;
        private Vector pos;

        private PinPos(VisualElement ve, boolean horizontal) {
            if (horizontal)
                orderPos = ve.getPos().x;
            else
                orderPos = ve.getPos().y;
            label = ve.getElementAttributes().getLabel();
        }

        @Override
        public int compareTo(PinPos pinPos) {
            return orderPos - pinPos.orderPos;
        }

    }

    private final static class PinList implements Iterable<PinPos> {
        private final boolean horizontal;
        private ArrayList<PinPos> pins;

        private PinList(boolean horizontal) {
            this.horizontal = horizontal;
            pins = new ArrayList<>();
        }

        private void add(VisualElement ve) {
            pins.add(new PinPos(ve, horizontal));
        }

        private int size() {
            return pins.size();
        }

        private void createPosition(HashMap<String, PinPos> map, Vector pos) {
            Collections.sort(pins);
            for (PinPos pp : pins) {
                map.put(pp.label, pp);
                pp.pos = pos;
                if (horizontal)
                    pos = pos.add(SIZE, 0);
                else
                    pos = pos.add(0, SIZE);
            }
        }

        @Override
        public Iterator<PinPos> iterator() {
            return pins.iterator();
        }
    }
}
