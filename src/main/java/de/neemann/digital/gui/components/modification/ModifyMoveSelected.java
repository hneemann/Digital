/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.CircuitSelection;
import de.neemann.digital.draw.elements.Movable;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Transform;
import de.neemann.digital.draw.graphics.TransformRotate;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.VectorFloat;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.Modification;

import java.util.ArrayList;
import java.util.function.Function;

/**
 * Modifier to move a selection
 */
public class ModifyMoveSelected implements Modification<Circuit> {
    private final Function<Circuit, ArrayList<Movable>> findMovables;
    private final Vector accumulatedDelta;
    private final int accumulatedRotate;
    private final Vector center;

    /**
     * Create a new instance
     *
     * @param min               the upper left corner
     * @param max               the lower right corner
     * @param accumulatedDelta  the translation
     * @param accumulatedRotate the rotation
     * @param center            the center of the selection rectangle
     */
    public ModifyMoveSelected(Vector min, Vector max, Vector accumulatedDelta, int accumulatedRotate, Vector center) {
        this(circuit -> circuit.getElementsToMove(min, max), accumulatedDelta, accumulatedRotate, center);
    }

    /**
     * Moves or rotates a selection of individual components and wire endpoints.
     * @param selection the selected objects before the edit
     * @param accumulatedDelta the translation
     * @param accumulatedRotate the number of quarter turns
     * @param center the rotation center after translation
     */
    public ModifyMoveSelected(CircuitSelection selection, Vector accumulatedDelta, int accumulatedRotate, Vector center) {
        this(new CircuitSelection(selection)::getMovables, accumulatedDelta, accumulatedRotate, center);
    }

    private ModifyMoveSelected(Function<Circuit, ArrayList<Movable>> findMovables,
                               Vector accumulatedDelta, int accumulatedRotate, Vector center) {
        this.findMovables = findMovables;
        this.accumulatedDelta = accumulatedDelta;
        this.accumulatedRotate = accumulatedRotate;
        this.center = center;
    }

    @Override
    public void modify(Circuit circuit) {
        ArrayList<Movable> list = findMovables.apply(circuit);
        if (list != null && !list.isEmpty()) {
            for (Movable m : list)
                m.move(accumulatedDelta);

            for (int i = 0; i < accumulatedRotate; i++)
                rotateElements(list, center);

            circuit.elementsMoved();
        }
    }

    /**
     * Rotates the given elements
     *
     * @param elements the elements to rotate
     * @param center   the center position
     */
    public static void rotateElements(ArrayList<Movable> elements, Vector center) {
        Transform transform = new TransformRotate(center, 1) {
            @Override
            public Vector transform(Vector v) {
                return super.transform(v.sub(center));
            }

            @Override
            public VectorFloat transform(VectorFloat v) {
                return super.transform(v.sub(center));
            }
        };

        for (Movable m : elements) {
            if (m instanceof VisualElement) {
                VisualElement ve = (VisualElement) m;
                ve.rotate();
                ve.setPos(transform.transform(ve.getPos()));
            } else if (m instanceof Wire) {
                Wire w = (Wire) m;
                w.p1 = transform.transform(w.p1);
                w.p2 = transform.transform(w.p2);
            } else {
                Vector p = m.getPos();
                Vector t = transform.transform(p);
                m.move(t.sub(p));
            }
        }
    }

    @Override
    public String toString() {
        return Lang.get("mod_movedSelected");
    }

}
