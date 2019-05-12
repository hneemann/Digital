/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.undo;

import junit.framework.TestCase;

public class UndoManagerTest extends TestCase {

    public static class MyString implements HistoryComponent<MyString> {
        private String text;

        private MyString(String text) {
            this.text = text;
        }

        @Override
        public MyString createDeepCopy() {
            return new MyString(text);
        }

        private void append(String app) {
            text = text + app;
        }
    }

    public static class Append implements Modification<MyString> {
        private final String app;

        public Append(String app) {
            this.app = app;
        }

        @Override
        public void modify(MyString myString) {
            myString.append(app);
        }

        @Override
        public String toString() {
            return "Append " + app;
        }
    }

    public void testSimple() throws ModifyException {
        UndoManager<MyString> mm = new UndoManager<>(new MyString("initial"));

        assertEquals("initial", mm.getActual().text);
        assertFalse(mm.undoAvailable());
        assertFalse(mm.redoAvailable());

        mm.apply(new Append("_1"));
        assertEquals("initial_1", mm.getActual().text);
        assertEquals("Append _1",mm.getUndoModification().toString());
        assertTrue(mm.undoAvailable());
        assertFalse(mm.redoAvailable());

        mm.apply(new Append("_2"));
        assertEquals("initial_1_2", mm.getActual().text);
        assertTrue(mm.undoAvailable());
        assertFalse(mm.redoAvailable());

        mm.undo();
        assertEquals("initial_1", mm.getActual().text);
        assertTrue(mm.undoAvailable());
        assertTrue(mm.redoAvailable());

        assertEquals("Append _2",mm.getRedoModification().toString());
        mm.redo();
        assertEquals("initial_1_2", mm.getActual().text);
        assertTrue(mm.undoAvailable());
        assertFalse(mm.redoAvailable());

        mm.undo();
        mm.undo();
        assertEquals("initial", mm.getActual().text);
        assertFalse(mm.undoAvailable());
        assertTrue(mm.redoAvailable());


        mm.redo();
        mm.redo();
        assertEquals("initial_1_2", mm.getActual().text);
        assertTrue(mm.undoAvailable());
        assertFalse(mm.redoAvailable());
    }

    public void testReModify() throws ModifyException {
        UndoManager<MyString> mm = new UndoManager<>(new MyString("initial"));
        mm.apply(new Append("_1"));
        mm.apply(new Append("_2"));
        mm.apply(new Append("_3"));
        assertEquals("initial_1_2_3", mm.getActual().text);
        assertTrue(mm.undoAvailable());
        assertFalse(mm.redoAvailable());

        mm.undo();
        mm.undo();
        assertEquals("initial_1", mm.getActual().text);
        assertTrue(mm.undoAvailable());
        assertTrue(mm.redoAvailable());

        mm.apply(new Append("_2b"));
        assertEquals("initial_1_2b", mm.getActual().text);
        assertTrue(mm.undoAvailable());
        assertFalse(mm.redoAvailable());
    }

    public void testSaved() throws ModifyException {
        UndoManager<MyString> mm = new UndoManager<>(new MyString("initial"));
        assertFalse(mm.isModified());
        mm.apply(new Append("_1"));
        mm.apply(new Append("_2"));
        assertEquals("initial_1_2", mm.getActual().text);
        assertTrue(mm.isModified());

        mm.saved();

        assertFalse(mm.isModified());
        mm.apply(new Append("_3"));
        assertTrue(mm.isModified());

        mm.undo();
        assertFalse(mm.isModified());
        mm.undo();
        assertTrue(mm.isModified());
        mm.redo();
        assertFalse(mm.isModified());
    }

    public void testConsistency() throws ModifyException {
        UndoManager<MyString> mm = new UndoManager<>(new MyString("initial"));
        mm.apply(new Append("_1"));
        assertEquals("initial_1", mm.getActual().text);
        mm.undo();
        assertEquals("initial", mm.getActual().text);
        mm.apply(new Append("_5"));
        assertEquals("initial_5", mm.getActual().text);
        mm.undo();
        assertEquals("initial", mm.getActual().text);
    }
}