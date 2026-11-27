/*
 * Copyright (c) 2026
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.ShapeFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A selection independent of its bounding rectangle.
 * Circuit list indices survive the deep copies used by undo history replay.
 * Wire endpoints are selected independently, preserving rectangular move behavior.
 * A selection is valid until the circuit is edited; history stores a private copy.
 */
public final class CircuitSelection {
    private static final int FIRST_ENDPOINT = 1;
    private static final int SECOND_ENDPOINT = 2;
    private static final int BOTH_ENDPOINTS = FIRST_ENDPOINT | SECOND_ENDPOINT;
    private final TreeSet<Integer> elements = new TreeSet<>();
    private final TreeMap<Integer, Integer> wires = new TreeMap<>();

    /** Creates an empty selection. */
    public CircuitSelection() {
    }

    /**
     * Copies a selection.
     * @param original the selection
     */
    public CircuitSelection(CircuitSelection original) {
        elements.addAll(original.elements);
        wires.putAll(original.wires);
    }

    /**
     * Finds components and wire endpoints inside a rectangle.
     * @param circuit the circuit
     * @param corner1 first corner
     * @param corner2 opposite corner
     * @return the matching selection
     */
    public static CircuitSelection rectangle(Circuit circuit, Vector corner1, Vector corner2) {
        CircuitSelection result = new CircuitSelection();
        result.addRectangle(circuit, corner1, corner2);
        return result;
    }

    /**
     * Adds components and wire endpoints in a rectangle to this selection.
     * @param circuit the circuit
     * @param corner1 first corner
     * @param corner2 opposite corner
     * @return true if a component or endpoint was added
     */
    public boolean addRectangle(Circuit circuit, Vector corner1, Vector corner2) {
        Vector min = Vector.min(corner1, corner2);
        Vector max = Vector.max(corner1, corner2);
        boolean changed = false;
        for (int i = 0; i < circuit.getElements().size(); i++)
            if (circuit.getElements().get(i).matches(min, max) && elements.add(i))
                changed = true;
        for (int i = 0; i < circuit.getWires().size(); i++) {
            Wire wire = circuit.getWires().get(i);
            int hits = (wire.p1.inside(min, max) ? FIRST_ENDPOINT : 0) | (wire.p2.inside(min, max) ? SECOND_ENDPOINT : 0);
            if (hits == 0) continue;
            int previous = wires.getOrDefault(i, 0);
            int mask = previous | hits;
            if (mask != previous) {
                wires.put(i, mask);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Changes the selection of one component.
     * @param circuit the circuit containing the component
     * @param element the component to add or remove
     */
    public void toggleComponent(Circuit circuit, VisualElement element) {
        int index = circuit.getElements().indexOf(element);
        if (index >= 0 && !elements.remove(index))
            elements.add(index);
    }

    /** @return true if no objects are selected */
    public boolean isEmpty() {
        return elements.isEmpty() && wires.isEmpty();
    }

    /**
     * Tests whether a component is selected.
     * @param circuit the circuit
     * @param element the component
     * @return true if selected
     */
    public boolean contains(Circuit circuit, VisualElement element) {
        return elements.contains(circuit.getElements().indexOf(element));
    }

    /**
     * Resolves the selected components in circuit order.
     * @param circuit the circuit
     * @return selected components
     */
    public ArrayList<VisualElement> getComponents(Circuit circuit) {
        ArrayList<VisualElement> result = new ArrayList<>(elements.size());
        for (Integer index : elements) result.add(circuit.getElements().get(index));
        return result;
    }

    /**
     * Resolves a single selected component for component-specific editing.
     * @param circuit the circuit
     * @return the component, or null for a group or wire selection
     */
    public VisualElement getSingleElement(Circuit circuit) {
        return elements.size() == 1 && wires.isEmpty() ? circuit.getElements().get(elements.first()) : null;
    }

    /**
     * Resolves selected objects on the current circuit or an undo replay copy.
     * @param circuit the circuit
     * @return objects to highlight or draw
     */
    public ArrayList<Drawable> getDrawables(Circuit circuit) {
        ArrayList<Drawable> result = new ArrayList<>();
        for (Integer index : elements) result.add(circuit.getElements().get(index));
        for (Integer index : wires.keySet()) result.add(circuit.getWires().get(index));
        return result;
    }

    /**
     * Resolves the exact components and endpoints to move.
     * @param circuit the circuit
     * @return selected movables
     */
    public ArrayList<Movable> getMovables(Circuit circuit) {
        ArrayList<Movable> result = new ArrayList<>();
        for (Integer index : elements) result.add(circuit.getElements().get(index));
        for (Map.Entry<Integer, Integer> entry : wires.entrySet())
            addWireEndpoints(result, circuit.getWires().get(entry.getKey()), entry.getValue());
        return result;
    }

    /**
     * Copies only the objects drawn in the move preview.
     * @param circuit the original circuit
     * @param factory shape factory
     * @return copied drawables and their movable components and endpoints
     */
    public Circuit.RectContainer copyForMove(Circuit circuit, ShapeFactory factory) {
        ArrayList<Drawable> drawables = new ArrayList<>();
        ArrayList<Movable> movables = new ArrayList<>();
        for (Integer index : elements) {
            VisualElement copy = new VisualElement(circuit.getElements().get(index)).setShapeFactory(factory);
            drawables.add(copy);
            movables.add(copy);
        }
        for (Map.Entry<Integer, Integer> entry : wires.entrySet()) {
            Wire copy = new Wire(circuit.getWires().get(entry.getKey()));
            drawables.add(copy);
            addWireEndpoints(movables, copy, entry.getValue());
        }
        return new Circuit.RectContainer(drawables, movables);
    }

    private static void addWireEndpoints(ArrayList<Movable> movables, Wire wire, int mask) {
        if ((mask & FIRST_ENDPOINT) != 0) movables.add(wire.getMovableP1());
        if ((mask & SECOND_ENDPOINT) != 0) movables.add(wire.getMovableP2());
    }

    /**
     * Copies components and fully selected wires for the clipboard.
     * @param circuit the circuit
     * @param factory shape factory
     * @return independent copies, or null for an empty clipboard selection
     */
    public ArrayList<Movable> copy(Circuit circuit, ShapeFactory factory) {
        ArrayList<Movable> result = new ArrayList<>();
        for (Integer index : elements)
            result.add(new VisualElement(circuit.getElements().get(index)).setShapeFactory(factory));
        for (Map.Entry<Integer, Integer> entry : wires.entrySet())
            if (entry.getValue() == BOTH_ENDPOINTS)
                result.add(new Wire(circuit.getWires().get(entry.getKey())));
        return result.isEmpty() ? null : result;
    }

    /**
     * Creates the stationary background for a move preview without normalizing wires.
     * @param circuit the original circuit
     * @return a shallow copy with the selected drawables omitted
     */
    public Circuit background(Circuit circuit) {
        Circuit result = circuit.createShallowCopy();
        removeFrom(result);
        return result;
    }

    /**
     * Deletes selected objects in a single batch, preserving list indices until done.
     * @param circuit the circuit to edit
     */
    public void deleteFrom(Circuit circuit) {
        removeFrom(circuit);
        if (!wires.isEmpty()) circuit.elementsMoved();
    }

    private void removeFrom(Circuit circuit) {
        for (Integer index : elements.descendingSet()) circuit.getElements().remove((int) index);
        for (Integer index : wires.descendingKeySet()) circuit.getWires().remove((int) index);
    }

    /**
     * Finds the center of the actual selected objects, including disjoint groups.
     * @param circuit the circuit
     * @return center, or the origin for an empty selection
     */
    public Vector center(Circuit circuit) {
        GraphicMinMax bounds = new GraphicMinMax();
        for (Drawable drawable : getDrawables(circuit)) drawable.drawTo(bounds, null);
        return bounds.getMin() == null ? new Vector(0, 0) : bounds.getMin().add(bounds.getMax()).div(2);
    }
}
