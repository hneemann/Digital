/*
 * Copyright (c) 2026
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.basic.Not;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.CircuitSelection;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.modification.ModifyDeleteSelection;
import de.neemann.digital.gui.components.modification.ModifyMoveSelected;
import de.neemann.digital.undo.UndoManager;
import junit.framework.TestCase;

public class CircuitSelectionTest extends TestCase {
    private ShapeFactory factory;
    private Circuit circuit;

    @Override
    protected void setUp() {
        factory = new ShapeFactory(new ElementLibrary());
        circuit = new Circuit();
        for (int x : new int[]{0, 100, 200})
            circuit.add(new VisualElement(Not.DESCRIPTION.getName()).setShapeFactory(factory).setPos(new Vector(x, 0)));
    }

    private CircuitSelection selectComponents(int... indices) {
        CircuitSelection selection = new CircuitSelection();
        for (int index : indices) selection.toggleComponent(circuit, circuit.getElements().get(index));
        return selection;
    }

    public void testDisjointSelectionAndSingleToggle() {
        CircuitSelection selection = selectComponents(0, 2);
        assertEquals(2, selection.getDrawables(circuit).size());
        assertFalse(selection.contains(circuit, circuit.getElements().get(1)));
        selection.toggleComponent(circuit, circuit.getElements().get(0));
        assertEquals(1, selection.getDrawables(circuit).size());
        assertTrue(selection.contains(circuit, circuit.getElements().get(2)));
    }

    public void testCopyExcludesUnselectedMiddle() {
        CircuitSelection selection = selectComponents(0, 2);
        assertEquals(2, selection.copy(circuit, factory).size());
        assertNotSame(circuit.getElements().get(0), selection.copy(circuit, factory).get(0));
    }

    public void testMoveUndoRedoAndDeleteReplay() throws Exception {
        UndoManager<Circuit> history = new UndoManager<>(circuit);
        CircuitSelection selection = selectComponents(0, 2);
        history.apply(new ModifyMoveSelected(selection, new Vector(0, 100), 0, new Vector(120, 100)));
        assertEquals(new Vector(0, 100), history.getActual().getElements().get(0).getPos());
        assertEquals(new Vector(100, 0), history.getActual().getElements().get(1).getPos());
        assertEquals(new Vector(200, 100), history.getActual().getElements().get(2).getPos());
        history.apply(new ModifyDeleteSelection(selection));
        assertEquals(1, history.getActual().getElements().size());
        assertEquals(new Vector(100, 0), history.getActual().getElements().get(0).getPos());
        history.undo();
        history.undo();
        assertEquals(new Vector(0, 0), history.getActual().getElements().get(0).getPos());
        history.redo();
        history.redo();
        assertEquals(1, history.getActual().getElements().size());
    }

    public void testModificationOwnsSelectionSnapshot() throws Exception {
        CircuitSelection selection = selectComponents(0);
        ModifyDeleteSelection modification = new ModifyDeleteSelection(selection);
        selection.toggleComponent(circuit, circuit.getElements().get(2));
        modification.modify(circuit);
        assertEquals(2, circuit.getElements().size());
        assertEquals(new Vector(200, 0), circuit.getElements().get(1).getPos());
    }

    public void testBoxMovesSelectedWireEndpointAndCopiesOnlyWholeWires() throws Exception {
        circuit.add(new Wire(new Vector(0, 100), new Vector(200, 100)));
        CircuitSelection endpoint = CircuitSelection.rectangle(circuit, new Vector(-10, 80), new Vector(10, 120));
        assertEquals(1, endpoint.getMovables(circuit).size());
        assertNull(endpoint.copy(circuit, factory));
        new ModifyMoveSelected(endpoint, new Vector(0, 20), 0, new Vector(0, 0)).modify(circuit);
        Wire wire = circuit.getWires().get(0);
        assertTrue(wire.p1.equals(new Vector(0, 120)) || wire.p2.equals(new Vector(0, 120)));
        assertTrue(wire.p1.equals(new Vector(200, 100)) || wire.p2.equals(new Vector(200, 100)));
        CircuitSelection wholeWire = CircuitSelection.rectangle(circuit, new Vector(-10, 80), new Vector(210, 140));
        assertEquals(2, wholeWire.getMovables(circuit).size());
        assertEquals(1, wholeWire.copy(circuit, factory).size());
    }

    public void testDeleteWiresInOneBatchAndReplay() throws Exception {
        circuit.add(new Wire(new Vector(0, 100), new Vector(100, 100)));
        circuit.add(new Wire(new Vector(0, 200), new Vector(100, 200)));
        circuit.add(new Wire(new Vector(0, 300), new Vector(100, 300)));
        CircuitSelection selection = CircuitSelection.rectangle(circuit, new Vector(-10, 80), new Vector(120, 220));
        UndoManager<Circuit> history = new UndoManager<>(circuit);
        history.apply(new ModifyDeleteSelection(selection));
        assertEquals(1, history.getActual().getWires().size());
        assertEquals(300, history.getActual().getWires().get(0).p1.y);
        history.undo();
        assertEquals(3, history.getActual().getWires().size());
        history.redo();
        assertEquals(1, history.getActual().getWires().size());
    }

    public void testBackgroundDoesNotMutateCircuit() {
        Circuit background = selectComponents(0).background(circuit);
        assertEquals(2, background.getElements().size());
        assertEquals(3, circuit.getElements().size());
    }

    public void testMovePreviewCopiesSelectedObjectsAndMovesOnlySelectedEndpoint() {
        circuit.add(new Wire(new Vector(0, 100), new Vector(200, 100)));
        CircuitSelection selection = CircuitSelection.rectangle(circuit, new Vector(-20, -30), new Vector(60, 120));
        Circuit.RectContainer preview = selection.copyForMove(circuit, factory);
        assertEquals(2, preview.getDrawables().size());
        assertEquals(2, preview.getMovables().size());
        preview.getMovables().forEach(movable -> movable.move(new Vector(0, 40)));

        VisualElement component = (VisualElement) preview.getDrawables().get(0);
        Wire wire = (Wire) preview.getDrawables().get(1);
        assertEquals(new Vector(0, 40), component.getPos());
        assertEquals(new Vector(0, 140), wire.p1);
        assertEquals(new Vector(200, 100), wire.p2);
        assertEquals(new Vector(0, 0), circuit.getElements().get(0).getPos());
        assertEquals(new Vector(0, 100), circuit.getWires().get(0).p1);
    }

    public void testRotationLeavesUnselectedComponentUntouched() throws Exception {
        CircuitSelection selection = selectComponents(0, 2);
        UndoManager<Circuit> history = new UndoManager<>(circuit);
        history.apply(new ModifyMoveSelected(selection, new Vector(0, 0), 1, selection.center(circuit)));
        assertEquals(0, history.getActual().getElements().get(1).getRotate());
        assertEquals(new Vector(100, 0), history.getActual().getElements().get(1).getPos());
        assertEquals(1, history.getActual().getElements().get(0).getRotate());
        history.undo();
        history.redo();
        assertEquals(1, history.getActual().getElements().get(2).getRotate());
    }
}
