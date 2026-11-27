/*
 * Copyright (c) 2026
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.basic.Not;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.gui.components.modification.ModifyDeleteElement;
import de.neemann.gui.ToolTipAction;
import junit.framework.TestCase;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.KeyboardFocusManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/** Exercises the actual mouse dispatcher on the EDT without opening a window. */
public class MultiSelectionInteractionTest extends TestCase {
    private CircuitComponent component;
    private Circuit circuit;
    private int selectionModifier;
    private int squareModifier;

    @Override
    protected void setUp() throws Exception {
        boolean macMouse = Settings.getInstance().get(Keys.SETTINGS_MAC_MOUSE);
        selectionModifier = macMouse ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
        squareModifier = macMouse ? InputEvent.SHIFT_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
        SwingUtilities.invokeAndWait(() -> {
            ElementLibrary library = new ElementLibrary();
            ShapeFactory factory = new ShapeFactory(library);
            circuit = new Circuit();
            for (int x : new int[]{100, 300, 500})
                circuit.add(new VisualElement(Not.DESCRIPTION.getName()).setShapeFactory(factory).setPos(new Vector(x, 100)));
            component = new CircuitComponent(null, library, factory) {
                @Override
                public boolean hasFocus() {
                    return true;
                }
            };
            component.setCircuit(circuit);
        });
    }

    private void event(int id, int x, int y, int modifiers, int button) {
        MouseEvent event = new MouseEvent(component, id, System.currentTimeMillis(), modifiers, x, y, 1, false, button);
        if (id == MouseEvent.MOUSE_DRAGGED || id == MouseEvent.MOUSE_MOVED) {
            for (MouseMotionListener listener : component.getMouseMotionListeners()) {
                if (id == MouseEvent.MOUSE_DRAGGED) listener.mouseDragged(event);
                else listener.mouseMoved(event);
            }
        } else {
            for (MouseListener listener : component.getMouseListeners()) {
                if (id == MouseEvent.MOUSE_PRESSED) listener.mousePressed(event);
                if (id == MouseEvent.MOUSE_RELEASED) listener.mouseReleased(event);
            }
        }
    }

    private void click(int x, int y, int modifiers) {
        event(MouseEvent.MOUSE_PRESSED, x, y, modifiers | InputEvent.BUTTON1_DOWN_MASK, MouseEvent.BUTTON1);
        event(MouseEvent.MOUSE_RELEASED, x, y, modifiers, MouseEvent.BUTTON1);
    }

    private void hover(int x, int y) {
        event(MouseEvent.MOUSE_MOVED, x, y, 0, MouseEvent.NOBUTTON);
    }

    private void assertComponentsStayAtOriginalPositions() {
        assertSame("Hovering must not activate a moving preview", component.getCircuit(), component.getCircuitOrShallowCopy());
        for (int i = 0; i < 3; i++)
            assertEquals(new Vector(100 + i * 200, 100), component.getCircuit().getElements().get(i).getPos());
        assertFalse(component.isModified());
    }

    private void drag(int x1, int y1, int x2, int y2, int modifiers) {
        event(MouseEvent.MOUSE_PRESSED, x1, y1, modifiers | InputEvent.BUTTON1_DOWN_MASK, MouseEvent.BUTTON1);
        event(MouseEvent.MOUSE_DRAGGED, (x1 + x2) / 2, (y1 + y2) / 2,
                modifiers | InputEvent.BUTTON1_DOWN_MASK, MouseEvent.NOBUTTON);
        event(MouseEvent.MOUSE_DRAGGED, x2, y2, modifiers | InputEvent.BUTTON1_DOWN_MASK, MouseEvent.NOBUTTON);
        event(MouseEvent.MOUSE_RELEASED, x2, y2, modifiers, MouseEvent.BUTTON1);
    }

    private void key(KeyStroke stroke) {
        Object key = component.getInputMap().get(stroke);
        Action action = component.getActionMap().get(key);
        assertNotNull(action);
        action.actionPerformed(null);
    }

    private void pressShift() {
        pressKey(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK);
    }

    private void pressSelectionModifier() {
        pressKey(selectionModifier == InputEvent.META_DOWN_MASK ? KeyEvent.VK_META : KeyEvent.VK_CONTROL, selectionModifier);
    }

    private void pressKey(int keyCode, int modifier) {
        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.redispatchEvent(component, new KeyEvent(component, KeyEvent.KEY_PRESSED, 0,
                modifier, keyCode, KeyEvent.CHAR_UNDEFINED));
        focusManager.redispatchEvent(component, new KeyEvent(component, KeyEvent.KEY_RELEASED, 1,
                0, keyCode, KeyEvent.CHAR_UNDEFINED));
    }

    public void testModifierAfterClickKeepsTheOriginalAndAddsAnotherComponent() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            click(120, 100, 0);
            hover(160, 220);
            assertNotSame(circuit, component.getCircuitOrShallowCopy());

            pressSelectionModifier();
            hover(620, 80);
            assertComponentsStayAtOriginalPositions();
            assertEquals(1, component.getHighLighted().size());
            assertTrue(component.getHighLighted().contains(circuit.getElements().get(0)));

            click(520, 100, selectionModifier);
            hover(620, 80);
            pressShift();
            hover(660, 200);
            click(660, 200, 0);
            assertEquals(new Vector(140, 220), component.getCircuit().getElements().get(0).getPos());
            assertEquals(new Vector(300, 100), component.getCircuit().getElements().get(1).getPos());
            assertEquals(new Vector(540, 220), component.getCircuit().getElements().get(2).getPos());
        });
    }

    public void testModifierAfterOrdinaryBoxKeepsItWhenAddingAnotherBox() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            circuit.add(new Wire(new Vector(300, 140), new Vector(700, 140)));
            Wire wire = circuit.getWires().get(0);
            drag(80, 60, 360, 150, 0);
            assertEquals(3, component.getHighLighted().size());

            pressSelectionModifier();
            drag(460, 60, 560, 150, selectionModifier);
            assertEquals(4, component.getHighLighted().size());
            assertTrue(component.getHighLighted().containsAll(circuit.getElements()));
            assertTrue(component.getHighLighted().contains(wire));
            assertComponentsStayAtOriginalPositions();
        });
    }

    public void testControlClickAddsAndRemovesSingleComponents() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            click(120, 100, selectionModifier);
            click(520, 100, selectionModifier);
            assertEquals(2, component.getHighLighted().size());
            assertFalse(component.getHighLighted().contains(circuit.getElements().get(1)));
            click(120, 100, selectionModifier);
            assertEquals(1, component.getHighLighted().size());
            click(520, 100, selectionModifier);
            assertTrue(component.getHighLighted().isEmpty());
        });
    }

    public void testOrdinaryClickPicksUpComponentUntilNextClick() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            click(120, 100, 0);
            hover(120, 220);
            assertNotSame(circuit, component.getCircuitOrShallowCopy());
            assertFalse(component.getCircuitOrShallowCopy().getElements().contains(circuit.getElements().get(0)));
            assertEquals(new Vector(100, 100), component.getCircuit().getElements().get(0).getPos());
            click(120, 220, 0);
            assertEquals(new Vector(100, 220), component.getCircuit().getElements().get(0).getPos());
            assertEquals(new Vector(300, 100), component.getCircuit().getElements().get(1).getPos());
            assertSame(component.getCircuit(), component.getCircuitOrShallowCopy());
        });
    }

    public void testControlClickSelectionsStayStillBetweenClicks() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            click(120, 100, selectionModifier);
            hover(320, 200);
            click(520, 100, selectionModifier);
            hover(600, 250);
            assertEquals(2, component.getHighLighted().size());
            assertComponentsStayAtOriginalPositions();
        });
    }

    public void testShiftPicksUpGroupFromCurrentCursorAndPlacesInOneUndoStep() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            circuit.add(new Wire(new Vector(100, 100), new Vector(300, 100)));
            click(120, 100, selectionModifier);
            click(520, 100, selectionModifier);
            hover(620, 80);
            pressShift();
            assertEquals(1, component.getCircuitOrShallowCopy().getElements().size());
            assertEquals(1, component.getCircuitOrShallowCopy().getWires().size());
            hover(660, 200);
            assertFalse(component.isModified());
            click(660, 200, 0);
            assertEquals(new Vector(140, 220), component.getCircuit().getElements().get(0).getPos());
            assertEquals(new Vector(300, 100), component.getCircuit().getElements().get(1).getPos());
            assertEquals(new Vector(540, 220), component.getCircuit().getElements().get(2).getPos());
            assertEquals(new Vector(100, 100), component.getCircuit().getWires().get(0).p1);
            assertEquals(new Vector(300, 100), component.getCircuit().getWires().get(0).p2);
            component.getUndoAction().actionPerformed(null);
            for (int i = 0; i < 3; i++)
                assertEquals(new Vector(100 + i * 200, 100), component.getCircuit().getElements().get(i).getPos());
            assertFalse(component.isModified());
            component.getRedoAction().actionPerformed(null);
            assertEquals(new Vector(540, 220), component.getCircuit().getElements().get(2).getPos());
        });
    }

    public void testEscapeCancelsGroupMove() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            click(120, 100, selectionModifier);
            click(520, 100, selectionModifier);
            pressShift();
            hover(620, 220);
            key(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
            assertComponentsStayAtOriginalPositions();
            assertTrue(component.getHighLighted().isEmpty());
        });
    }

    public void testControlClickAndTwoControlBoxesMoveTogetherWithShift() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            circuit.add(new Wire(new Vector(300, 220), new Vector(500, 220)));
            Wire wire = circuit.getWires().get(0);
            click(120, 100, selectionModifier);

            drag(260, 60, 360, 240, selectionModifier);
            assertEquals(3, component.getHighLighted().size());
            assertTrue(component.getHighLighted().contains(circuit.getElements().get(0)));
            assertTrue(component.getHighLighted().contains(circuit.getElements().get(1)));
            assertTrue(component.getHighLighted().contains(wire));

            drag(560, 240, 460, 60, selectionModifier);
            assertEquals(4, component.getHighLighted().size());
            assertTrue(component.getHighLighted().containsAll(circuit.getElements()));
            assertTrue(component.getHighLighted().contains(wire));
            assertComponentsStayAtOriginalPositions();

            hover(620, 80);
            pressShift();
            hover(660, 200);
            click(660, 200, 0);
            assertEquals(new Vector(140, 220), component.getCircuit().getElements().get(0).getPos());
            assertEquals(new Vector(340, 220), component.getCircuit().getElements().get(1).getPos());
            assertEquals(new Vector(540, 220), component.getCircuit().getElements().get(2).getPos());
            Wire moved = component.getCircuit().getWires().get(0);
            assertEquals(new Vector(340, 340), moved.p1);
            assertEquals(new Vector(540, 340), moved.p2);
            component.getUndoAction().actionPerformed(null);
            assertComponentsStayAtOriginalPositions();
        });
    }

    public void testControlBoxUsesFinalBoundsAndPreservesSelectionOnOverlap() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            int controlDrag = selectionModifier | InputEvent.BUTTON1_DOWN_MASK;
            event(MouseEvent.MOUSE_PRESSED, 80, 60, controlDrag, MouseEvent.BUTTON1);
            event(MouseEvent.MOUSE_DRAGGED, 560, 140, controlDrag, MouseEvent.NOBUTTON);
            assertEquals(3, component.getHighLighted().size());
            event(MouseEvent.MOUSE_DRAGGED, 360, 140, InputEvent.BUTTON1_DOWN_MASK, MouseEvent.NOBUTTON);
            event(MouseEvent.MOUSE_RELEASED, 360, 140, 0, MouseEvent.BUTTON1);
            assertEquals(2, component.getHighLighted().size());
            assertFalse(component.getHighLighted().contains(circuit.getElements().get(2)));

            drag(260, 50, 380, 150, selectionModifier);
            assertEquals(2, component.getHighLighted().size());
            click(320, 100, selectionModifier);
            assertEquals(1, component.getHighLighted().size());
            assertTrue(component.getHighLighted().contains(circuit.getElements().get(0)));
            assertComponentsStayAtOriginalPositions();
        });
    }

    public void testEmptyOrTooThinControlBoxKeepsPreviousSelection() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            circuit.add(new Wire(new Vector(460, 180), new Vector(600, 180)));
            click(120, 100, selectionModifier);
            drag(620, 220, 700, 280, selectionModifier);
            drag(420, 178, 540, 183, selectionModifier);
            assertEquals(1, component.getHighLighted().size());
            assertTrue(component.getHighLighted().contains(circuit.getElements().get(0)));
            assertComponentsStayAtOriginalPositions();
        });
    }

    public void testPressingModifierDuringPlainDragConstrainsSquare() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            int controlDrag = squareModifier | InputEvent.BUTTON1_DOWN_MASK;
            event(MouseEvent.MOUSE_PRESSED, 80, 60, InputEvent.BUTTON1_DOWN_MASK, MouseEvent.BUTTON1);
            event(MouseEvent.MOUSE_DRAGGED, 280, 140, InputEvent.BUTTON1_DOWN_MASK, MouseEvent.NOBUTTON);
            pressKey(squareModifier == InputEvent.SHIFT_DOWN_MASK ? KeyEvent.VK_SHIFT : KeyEvent.VK_CONTROL, squareModifier);
            event(MouseEvent.MOUSE_DRAGGED, 360, 140, controlDrag, MouseEvent.NOBUTTON);
            event(MouseEvent.MOUSE_RELEASED, 360, 140, squareModifier, MouseEvent.BUTTON1);
            assertEquals(1, component.getHighLighted().size());
            assertTrue(component.getHighLighted().contains(circuit.getElements().get(0)));
            pressShift();
            assertComponentsStayAtOriginalPositions();
        });
    }

    public void testRotationCommitDoesNotClickThroughIntoAnotherAction() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            click(120, 100, selectionModifier);
            component.getRotateAction().actionPerformed(null);
            click(120, 100, 0);
            hover(500, 300);
            assertSame(component.getCircuit(), component.getCircuitOrShallowCopy());
            assertEquals(1, component.getCircuit().getElements().get(0).getRotate());
            assertTrue(component.getHighLighted().isEmpty());
        });
    }

    public void testDeleteOnlySelectedObjectsAndUndo() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            click(120, 100, selectionModifier);
            click(520, 100, selectionModifier);
            component.getDeleteAction().actionPerformed(null);
            assertEquals(1, component.getCircuit().getElements().size());
            assertEquals(new Vector(300, 100), component.getCircuit().getElements().get(0).getPos());
            component.getUndoAction().actionPerformed(null);
            assertEquals(3, component.getCircuit().getElements().size());
        });
    }

    public void testControlClickWireRetainsOriginalMove() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            circuit.add(new Wire(new Vector(100, 300), new Vector(300, 300)));
            click(200, 300, squareModifier);
            hover(200, 400);
            assertTrue(component.getCircuitOrShallowCopy().getWires().isEmpty());
            click(200, 400, 0);
            assertEquals(new Vector(100, 400), component.getCircuit().getWires().get(0).p1);
            assertEquals(new Vector(300, 400), component.getCircuit().getWires().get(0).p2);
        });
    }

    public void testCircuitEditInvalidatesSelectionIndices() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            click(520, 100, selectionModifier);
            assertEquals(1, component.getHighLighted().size());
            component.modify(new ModifyDeleteElement(circuit.getElements().get(0)));
            assertEquals(2, component.getCircuit().getElements().size());
            assertTrue(component.getHighLighted().isEmpty());
            pressShift();
            assertSame(component.getCircuit(), component.getCircuitOrShallowCopy());
        });
    }

    public void testSelectAllFromPartialSelectionAndEscape() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            click(120, 100, selectionModifier);
            key(KeyStroke.getKeyStroke(KeyEvent.VK_A, ToolTipAction.getCTRLMask()));
            assertEquals(3, component.getHighLighted().size());
            key(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
            assertTrue(component.getHighLighted().isEmpty());
        });
    }

}
