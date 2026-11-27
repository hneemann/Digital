/*
 * Copyright (c) 2026
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.CircuitSelection;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import java.util.ArrayList;

/**
 * Tracks the selection and the dashed outlines shown while editing it.
 */
final class CircuitSelectionState {
    private final ArrayList<Box> addedBoxes = new ArrayList<>();
    private CircuitSelection selection = new CircuitSelection();
    private CircuitSelection beforeBox;
    private Box box;
    private boolean additive;
    private boolean boxAddsObjects;

    CircuitSelection getSelection() {
        return selection;
    }

    boolean isAdditive() {
        return additive;
    }

    boolean isDraggingBox() {
        return beforeBox != null;
    }

    void toggleComponent(Circuit circuit, VisualElement component) {
        enableAdding();
        if (selection.contains(circuit, component))
            addedBoxes.removeIf(area -> area.includes(component));
        selection.toggleComponent(circuit, component);
    }

    void startBox(Vector start, boolean add) {
        if (add)
            enableAdding();
        else
            reset();
        beforeBox = selection;
        box = new Box(start);
    }

    void enableAdding() {
        if (!additive && box != null) {
            addedBoxes.add(box);
            box = null;
        }
        additive = true;
    }

    void updateBox(Circuit circuit, Vector end, boolean square) {
        box.updateEnd(end, square && !additive);
        // Only the current rectangle changes during a drag.
        selection = new CircuitSelection(beforeBox);
        boxAddsObjects = selection.addRectangle(circuit, box.start, box.end);
    }

    void finishBox(boolean allowSmallBox) {
        if (!allowSmallBox && !box.isLargeEnough()) {
            selection = beforeBox;
            box = null;
        } else if (additive) {
            if (boxAddsObjects) addedBoxes.add(box);
            box = null;
        }
        beforeBox = null;
    }

    Vector center(Circuit circuit) {
        return additive ? selection.center(circuit) : box.start.add(box.end).div(2);
    }

    void drawTo(Graphic graphic) {
        for (Box area : addedBoxes) area.drawTo(graphic);
        if (box != null) box.drawTo(graphic);
    }

    void reset() {
        selection = new CircuitSelection();
        beforeBox = null;
        box = null;
        addedBoxes.clear();
        additive = false;
    }

    private static final class Box {
        private static final int MIN_SIZE = 8;
        private final Vector start;
        private Vector end;

        private Box(Vector start) {
            this.start = start;
            end = start;
        }

        private void updateEnd(Vector end, boolean square) {
            if (square) {
                Vector delta = end.sub(start);
                int size = Math.min(Math.abs(delta.x), Math.abs(delta.y));
                this.end = start.add(Integer.signum(delta.x) * size, Integer.signum(delta.y) * size);
            } else
                this.end = end;
        }

        private boolean isLargeEnough() {
            Vector size = end.sub(start);
            return Math.abs(size.x) > MIN_SIZE && Math.abs(size.y) > MIN_SIZE;
        }

        private boolean includes(VisualElement element) {
            return element.matches(Vector.min(start, end), Vector.max(start, end));
        }

        private void drawTo(Graphic graphic) {
            Vector p1 = new Vector(start.x, end.y);
            Vector p2 = new Vector(end.x, start.y);
            graphic.drawLine(start, p1, Style.DASH);
            graphic.drawLine(p1, end, Style.DASH);
            graphic.drawLine(p2, end, Style.DASH);
            graphic.drawLine(start, p2, Style.DASH);
        }
    }
}
