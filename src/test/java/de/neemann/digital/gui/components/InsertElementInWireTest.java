/*
 * Copyright (c) 2026
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.basic.And;
import de.neemann.digital.core.basic.Not;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.undo.Modification;
import de.neemann.digital.undo.ModifyException;
import de.neemann.digital.undo.UndoManager;
import junit.framework.TestCase;

public class InsertElementInWireTest extends TestCase {
    private ShapeFactory shapeFactory;

    @Override
    protected void setUp() {
        ElementLibrary library = new ElementLibrary();
        shapeFactory = new ShapeFactory(library);
    }

    public void testInsertNotIntoHorizontalWire() throws ModifyException {
        Circuit circuit = new Circuit();
        Wire original = new Wire(new Vector(0, 0), new Vector(120, 0));
        circuit.add(original);
        VisualElement not = createNot();

        Modification<Circuit> modification = InsertElementInWire.alignAndCreate(
                circuit, not, original, new Vector(60, 0));

        assertNotNull(modification);
        assertEquals(new Vector(0, 0), not.getPos());
        modification.modify(circuit);
        assertEquals(1, circuit.getElements().size());
        assertEquals(new Vector(40, 0), circuit.getElements().get(0).getPos());
        assertEquals(2, circuit.getWires().size());
        assertTrue(hasWire(circuit, new Vector(0, 0), new Vector(40, 0)));
        assertTrue(hasWire(circuit, new Vector(80, 0), new Vector(120, 0)));
    }

    public void testInsertNotIntoVerticalWire() throws ModifyException {
        Circuit circuit = new Circuit();
        Wire original = new Wire(new Vector(0, 0), new Vector(0, 120));
        circuit.add(original);
        VisualElement not = createNot().setRotation(1);

        Modification<Circuit> modification = InsertElementInWire.alignAndCreate(
                circuit, not, original, new Vector(0, 60));

        assertNotNull(modification);
        modification.modify(circuit);
        assertEquals(1, circuit.getElements().size());
        assertEquals(2, circuit.getWires().size());
        assertTrue(hasWire(circuit, new Vector(0, 0), new Vector(0, 40)));
        assertTrue(hasWire(circuit, new Vector(0, 80), new Vector(0, 120)));
    }

    public void testRejectsElementWithoutOneInputAndOneOutput() {
        Circuit circuit = new Circuit();
        Wire original = new Wire(new Vector(0, 0), new Vector(120, 0));
        circuit.add(original);
        VisualElement and = new VisualElement(And.DESCRIPTION.getName()).setShapeFactory(shapeFactory);

        assertNull(InsertElementInWire.alignAndCreate(circuit, and, original, new Vector(60, 0)));
    }

    public void testRejectsBranchHiddenByElement() {
        Circuit circuit = new Circuit();
        Wire original = new Wire(new Vector(0, 0), new Vector(120, 0));
        circuit.add(original);
        circuit.add(new Wire(new Vector(60, 0), new Vector(60, 40)));
        VisualElement not = createNot();

        assertNull(InsertElementInWire.alignAndCreate(circuit, not, original, new Vector(60, 0)));
        assertEquals(new Vector(0, 0), not.getPos());
    }

    public void testInsertionIsOneUndoableModification() throws ModifyException {
        Circuit circuit = new Circuit();
        Wire original = new Wire(new Vector(0, 0), new Vector(120, 0));
        circuit.add(original);
        VisualElement not = createNot();
        Modification<Circuit> modification = InsertElementInWire.alignAndCreate(
                circuit, not, original, new Vector(60, 0));
        UndoManager<Circuit> undoManager = new UndoManager<>(circuit);

        assertNotNull(modification);
        undoManager.apply(modification);
        assertEquals(1, undoManager.getActual().getElements().size());
        assertEquals(2, undoManager.getActual().getWires().size());

        undoManager.undo();
        assertEquals(0, undoManager.getActual().getElements().size());
        assertEquals(1, undoManager.getActual().getWires().size());
        assertTrue(hasWire(undoManager.getActual(), new Vector(0, 0), new Vector(120, 0)));
        assertFalse(undoManager.undoAvailable());

        undoManager.redo();
        assertEquals(1, undoManager.getActual().getElements().size());
        assertEquals(2, undoManager.getActual().getWires().size());
        assertTrue(hasWire(undoManager.getActual(), new Vector(0, 0), new Vector(40, 0)));
        assertTrue(hasWire(undoManager.getActual(), new Vector(80, 0), new Vector(120, 0)));
    }

    public void testRejectsShortDiagonalAndMisalignedWires() {
        Wire[] wires = {
                new Wire(new Vector(0, 0), new Vector(20, 0)),
                new Wire(new Vector(0, 0), new Vector(120, 120)),
                new Wire(new Vector(0, 0), new Vector(0, 120))
        };
        for (Wire wire : wires) {
            Circuit circuit = new Circuit();
            circuit.add(wire);
            VisualElement not = createNot();
            assertNull(InsertElementInWire.alignAndCreate(circuit, not, wire, new Vector(60, 0)));
            assertEquals(new Vector(0, 0), not.getPos());
            assertEquals(0, circuit.getElements().size());
            assertEquals(1, circuit.getWires().size());
        }
    }

    public void testExactFitRemovesEntireWire() throws ModifyException {
        Circuit circuit = new Circuit();
        Wire wire = new Wire(new Vector(0, 0), new Vector(40, 0));
        circuit.add(wire);
        Modification<Circuit> modification = InsertElementInWire.alignAndCreate(
                circuit, createNot(), wire, new Vector(20, 0));
        assertNotNull(modification);
        modification.modify(circuit);
        assertEquals(0, circuit.getWires().size());
        assertEquals(1, circuit.getElements().size());
    }

    public void testClampsAtEitherEndWithReversedGateAndWire() throws ModifyException {
        for (int x : new int[]{0, 120}) {
            Circuit circuit = new Circuit();
            Wire wire = new Wire(new Vector(120, 0), new Vector(0, 0));
            circuit.add(wire);
            Modification<Circuit> modification = InsertElementInWire.alignAndCreate(
                    circuit, createNot().setRotation(2), wire, new Vector(x, 0));
            assertNotNull(modification);
            modification.modify(circuit);
            assertEquals(1, circuit.getElements().size());
            assertEquals(1, circuit.getWires().size());
            assertTrue(hasWire(circuit, new Vector(x == 0 ? 40 : 0, 0), new Vector(x == 0 ? 120 : 80, 0)));
        }
    }

    public void testRejectsHiddenExistingPin() {
        Circuit circuit = new Circuit();
        Wire wire = new Wire(new Vector(0, 0), new Vector(120, 0));
        circuit.add(wire);
        circuit.add(createNot().setPos(new Vector(60, 0)));
        assertNull(InsertElementInWire.alignAndCreate(circuit, createNot(), wire, new Vector(60, 0)));
    }

    public void testRejectsNewJunctionAtCrossingWire() {
        Circuit circuit = new Circuit();
        Wire wire = new Wire(new Vector(0, 0), new Vector(120, 0));
        circuit.add(wire);
        circuit.add(new Wire(new Vector(40, -40), new Vector(40, 40)));
        assertNull(InsertElementInWire.alignAndCreate(circuit, createNot(), wire, new Vector(60, 0)));
    }

    public void testKeepsUnconnectedCrossingBetweenPins() throws ModifyException {
        Circuit circuit = new Circuit();
        Wire wire = new Wire(new Vector(0, 0), new Vector(120, 0));
        circuit.add(wire);
        circuit.add(new Wire(new Vector(60, -40), new Vector(60, 40)));
        Modification<Circuit> modification = InsertElementInWire.alignAndCreate(
                circuit, createNot(), wire, new Vector(60, 0));
        assertNotNull(modification);
        modification.modify(circuit);
        assertEquals(3, circuit.getWires().size());
        assertTrue(hasWire(circuit, new Vector(60, -40), new Vector(60, 40)));
    }

    public void testInsertedGateInvertsSignalInSimulation() throws Exception {
        Circuit circuit = new Circuit();
        circuit.add(new VisualElement(In.DESCRIPTION.getName()).setShapeFactory(shapeFactory));
        circuit.add(new VisualElement(Out.DESCRIPTION.getName()).setShapeFactory(shapeFactory)
                .setPos(new Vector(120, 0)));
        Wire wire = new Wire(new Vector(0, 0), new Vector(120, 0));
        circuit.add(wire);
        Modification<Circuit> modification = InsertElementInWire.alignAndCreate(
                circuit, createNot(), wire, new Vector(60, 0));
        assertNotNull(modification);
        modification.modify(circuit);

        ModelCreator creator = new ModelCreator(circuit, new ElementLibrary());
        TestExecuter test = new TestExecuter(creator.createModel(false))
                .setInputs(creator.getEntries("In")).setOutputs(creator.getEntries("Out"));
        test.check(0, 1);
        test.check(1, 0);
        test.check(0, 1);
    }

    private VisualElement createNot() {
        return new VisualElement(Not.DESCRIPTION.getName()).setShapeFactory(shapeFactory);
    }

    public void testPreviewOverlapFromBothSidesAndAllRotations() throws ModifyException {
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int offset : new int[]{-5, 0, 5}) {
                boolean horizontal = rotation % 2 == 0;
                Circuit circuit = new Circuit();
                circuit.add(new Wire(new Vector(-120, 0), new Vector(120, 0)));
                if (!horizontal) {
                    circuit = new Circuit();
                    circuit.add(new Wire(new Vector(0, -120), new Vector(0, 120)));
                }
                VisualElement not = createNot().setRotation(rotation)
                        .setPos(horizontal ? new Vector(0, offset) : new Vector(offset, 0));
                Vector originalPosition = not.getPos();
                Modification<Circuit> insertion = InsertElementInWire.createAtPreview(circuit, not);
                assertNotNull("rotation=" + rotation + ", offset=" + offset, insertion);
                insertion.modify(circuit);
                assertEquals(originalPosition, not.getPos());
                assertEquals(1, circuit.getElements().size());
                assertEquals(2, circuit.getWires().size());
                assertEquals(0, horizontal ? circuit.getElements().get(0).getPos().y
                        : circuit.getElements().get(0).getPos().x);
            }
        }
    }

    public void testPreviewAwayFromWireDoesNotInsert() {
        Circuit circuit = new Circuit();
        circuit.add(new Wire(new Vector(0, 0), new Vector(120, 0)));
        assertNull(InsertElementInWire.createAtPreview(circuit, createNot().setPos(new Vector(40, 60))));
    }

    public void testPreviewChoosesNearestParallelWire() throws ModifyException {
        Circuit circuit = new Circuit();
        circuit.add(new Wire(new Vector(0, 8), new Vector(120, 8)));
        circuit.add(new Wire(new Vector(0, 0), new Vector(120, 0)));
        Modification<Circuit> insertion = InsertElementInWire.createAtPreview(
                circuit, createNot().setPos(new Vector(40, 0)));
        assertNotNull(insertion);
        insertion.modify(circuit);
        assertTrue(hasWire(circuit, new Vector(0, 8), new Vector(120, 8)));
        assertTrue(hasWire(circuit, new Vector(0, 0), new Vector(40, 0)));
    }

    public void testPreviewRejectsAmbiguousParallelWires() {
        Circuit circuit = new Circuit();
        circuit.add(new Wire(new Vector(0, -5), new Vector(120, -5)));
        circuit.add(new Wire(new Vector(0, 5), new Vector(120, 5)));
        assertNull(InsertElementInWire.createAtPreview(circuit, createNot().setPos(new Vector(40, 0))));
        assertEquals(2, circuit.getWires().size());
        assertEquals(0, circuit.getElements().size());
    }

    public void testPreviewInsertsBetweenLogicGates() throws Exception {
        Circuit circuit = new Circuit();
        circuit.add(new VisualElement(In.DESCRIPTION.getName()).setShapeFactory(shapeFactory)
                .setPos(new Vector(-40, 0)));
        circuit.add(createNot());
        circuit.add(createNot().setPos(new Vector(160, 0)));
        circuit.add(new VisualElement(Out.DESCRIPTION.getName()).setShapeFactory(shapeFactory)
                .setPos(new Vector(240, 0)));
        circuit.add(new Wire(new Vector(-40, 0), new Vector(0, 0)));
        circuit.add(new Wire(new Vector(40, 0), new Vector(160, 0)));
        circuit.add(new Wire(new Vector(200, 0), new Vector(240, 0)));
        Modification<Circuit> insertion = InsertElementInWire.createAtPreview(
                circuit, createNot().setPos(new Vector(80, 0)));
        assertNotNull(insertion);
        insertion.modify(circuit);
        ModelCreator creator = new ModelCreator(circuit, new ElementLibrary());
        TestExecuter test = new TestExecuter(creator.createModel(false))
                .setInputs(creator.getEntries("In")).setOutputs(creator.getEntries("Out"));
        test.check(0, 1);
        test.check(1, 0);
    }

    private boolean hasWire(Circuit circuit, Vector first, Vector second) {
        for (Wire wire : circuit.getWires())
            if (wire.p1.equals(first) && wire.p2.equals(second)
                    || wire.p1.equals(second) && wire.p2.equals(first))
                return true;
        return false;
    }
}
