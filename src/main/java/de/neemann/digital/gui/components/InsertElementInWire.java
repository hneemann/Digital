/*
 * Copyright (c) 2026
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.gui.components.modification.ModifyDeleteWire;
import de.neemann.digital.gui.components.modification.ModifyInsertElement;
import de.neemann.digital.gui.components.modification.ModifyInsertWire;
import de.neemann.digital.undo.Modification;
import de.neemann.digital.undo.Modifications;

/**
 * Creates the modification needed to insert a two-pin element into a wire.
 */
final class InsertElementInWire {
    private InsertElementInWire() {
    }

    /**
     * Finds an insertable wire covered by the preview, independently of the mouse anchor.
     * Equally close candidates are ambiguous and are not modified.
     *
     * @param circuit the circuit to modify
     * @param element the component preview
     * @return an insertion, or null if there is no unambiguous valid wire
     */
    static Modification<Circuit> createAtPreview(Circuit circuit, VisualElement element) {
        Pin[] pins = getInlinePins(element);
        if (pins == null)
            return null;
        GraphicMinMax bounds = element.getMinMax(false);
        Vector min = bounds.getMin();
        Vector max = bounds.getMax();
        if (min == null || max == null)
            return null;
        Vector center = pins[0].getPos().add(pins[1].getPos()).div(2);
        Modification<Circuit> best = null;
        long bestDistance = Long.MAX_VALUE;
        boolean ambiguous = false;
        for (Wire wire : circuit.getWires()) {
            long distance;
            if (wire.p1.y == wire.p2.y && pins[0].getPos().y == pins[1].getPos().y
                    && min.y <= wire.p1.y && wire.p1.y <= max.y
                    && Math.max(wire.p1.x, wire.p2.x) >= min.x && Math.min(wire.p1.x, wire.p2.x) <= max.x) {
                distance = Math.abs((long) center.y - wire.p1.y);
            } else if (wire.p1.x == wire.p2.x && pins[0].getPos().x == pins[1].getPos().x
                    && min.x <= wire.p1.x && wire.p1.x <= max.x
                    && Math.max(wire.p1.y, wire.p2.y) >= min.y && Math.min(wire.p1.y, wire.p2.y) <= max.y) {
                distance = Math.abs((long) center.x - wire.p1.x);
            } else {
                continue;
            }
            Modification<Circuit> candidate = alignAndCreate(circuit, element, wire, center);
            if (candidate != null) {
                if (distance < bestDistance) {
                    best = candidate;
                    bestDistance = distance;
                    ambiguous = false;
                } else if (distance == bestDistance) {
                    ambiguous = true;
                }
            }
        }
        return ambiguous ? null : best;
    }

    /**
     * Aligns a copy of the element with the wire near the preferred position and creates
     * a single undoable modification which inserts it into the wire.
     *
     * @param circuit           the circuit to modify
     * @param element           the element to insert
     * @param wire              the wire to insert the element into
     * @param preferredPosition the preferred center position of the element
     * @return the modification, or {@code null} if the element cannot be inserted
     */
    static Modification<Circuit> alignAndCreate(Circuit circuit, VisualElement element,
                                               Wire wire, Vector preferredPosition) {
        wire = findWire(circuit, wire);
        if (wire == null)
            return null;
        element = new VisualElement(element);
        Pin[] pins = getInlinePins(element);
        if (pins == null || !align(element, pins, wire, preferredPosition))
            return null;

        return create(circuit, element, wire);
    }

    private static Wire findWire(Circuit circuit, Wire selected) {
        for (Wire wire : circuit.getWires())
            if (wire.equalsContent(selected)
                    || wire.p1.equals(selected.p2) && wire.p2.equals(selected.p1))
                return wire;
        return null;
    }

    /**
     * Creates a single undoable modification for an already aligned element.
     *
     * @param circuit the circuit to modify
     * @param element the element to insert
     * @param wire    the wire to insert the element into
     * @return the modification, or {@code null} if the placement is invalid
     */
    private static Modification<Circuit> create(Circuit circuit, VisualElement element, Wire wire) {
        Pin[] pins = getInlinePins(element);
        if (pins == null || !wire.isPosOnWire(pins[0].getPos()) || !wire.isPosOnWire(pins[1].getPos()))
            return null;

        if (hasConnectionBetweenPins(circuit, wire, pins[0].getPos(), pins[1].getPos()))
            return null;

        Pin first = pins[0];
        Pin second = pins[1];
        if (distanceSquared(wire.p1, first.getPos()) + distanceSquared(wire.p2, second.getPos())
                > distanceSquared(wire.p1, second.getPos()) + distanceSquared(wire.p2, first.getPos())) {
            first = pins[1];
            second = pins[0];
        }

        ModifyInsertElement insertElement = new ModifyInsertElement(element);
        return new Modifications.Builder<Circuit>(insertElement.toString())
                .add(new ModifyDeleteWire(wire))
                .add(insertElement)
                .add(new ModifyInsertWire(new Wire(wire.p1, first.getPos())).checkIfLenZero())
                .add(new ModifyInsertWire(new Wire(second.getPos(), wire.p2)).checkIfLenZero())
                .build();
    }

    private static Pin[] getInlinePins(VisualElement element) {
        Pins pins = element.getPins();
        if (pins.size() != 2)
            return null;

        Pin first = pins.get(0);
        Pin second = pins.get(1);
        if (!PinDescription.Direction.isInOut(first.getDirection(), second.getDirection()))
            return null;
        if (first.getPos().equals(second.getPos()))
            return null;

        return new Pin[]{first, second};
    }

    private static boolean align(VisualElement element, Pin[] pins, Wire wire, Vector preferredPosition) {
        Vector first = pins[0].getPos();
        Vector second = pins[1].getPos();

        if (wire.p1.y == wire.p2.y && first.y == second.y) {
            int wireMin = Math.min(wire.p1.x, wire.p2.x);
            int wireMax = Math.max(wire.p1.x, wire.p2.x);
            int pinMin = Math.min(first.x, second.x);
            int pinMax = Math.max(first.x, second.x);
            if (pinMax - pinMin > wireMax - wireMin)
                return false;

            int center = (pinMin + pinMax) / 2;
            int target = clamp(preferredPosition.x, wireMin + center - pinMin, wireMax - pinMax + center);
            element.move(new Vector(target - center, wire.p1.y - first.y));
        } else if (wire.p1.x == wire.p2.x && first.x == second.x) {
            int wireMin = Math.min(wire.p1.y, wire.p2.y);
            int wireMax = Math.max(wire.p1.y, wire.p2.y);
            int pinMin = Math.min(first.y, second.y);
            int pinMax = Math.max(first.y, second.y);
            if (pinMax - pinMin > wireMax - wireMin)
                return false;

            int center = (pinMin + pinMax) / 2;
            int target = clamp(preferredPosition.y, wireMin + center - pinMin, wireMax - pinMax + center);
            element.move(new Vector(wire.p1.x - first.x, target - center));
        } else {
            return false;
        }

        pins = getInlinePins(element);
        return pins != null && wire.isPosOnWire(pins[0].getPos()) && wire.isPosOnWire(pins[1].getPos());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private static long distanceSquared(Vector first, Vector second) {
        long dx = (long) first.x - second.x;
        long dy = (long) first.y - second.y;
        return dx * dx + dy * dy;
    }

    private static boolean hasConnectionBetweenPins(Circuit circuit, Wire replacedWire,
                                                    Vector first, Vector second) {
        for (VisualElement existing : circuit.getElements())
            for (Pin pin : existing.getPins())
                if (strictlyBetween(pin.getPos(), first, second))
                    return true;

        for (Wire wire : circuit.getWires()) {
            if (wire.equalsContent(replacedWire))
                continue;
            if (strictlyBetween(wire.p1, first, second) || strictlyBetween(wire.p2, first, second))
                return true;
            // A pin on the interior of a crossing wire would create a new junction.
            if (createsJunction(wire, replacedWire, first) || createsJunction(wire, replacedWire, second))
                return true;
        }
        return false;
    }

    private static boolean createsJunction(Wire wire, Wire replacedWire, Vector pin) {
        return !pin.equals(replacedWire.p1) && !pin.equals(replacedWire.p2)
                && !pin.equals(wire.p1) && !pin.equals(wire.p2)
                && wire.distance(pin) < 0.1;
    }

    private static boolean strictlyBetween(Vector point, Vector first, Vector second) {
        if (first.x == second.x && point.x == first.x)
            return Math.min(first.y, second.y) < point.y && point.y < Math.max(first.y, second.y);
        if (first.y == second.y && point.y == first.y)
            return Math.min(first.x, second.x) < point.x && point.x < Math.max(first.x, second.x);
        return false;
    }
}
