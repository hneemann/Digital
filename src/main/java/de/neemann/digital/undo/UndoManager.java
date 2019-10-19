/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.undo;

import java.util.ArrayList;

/**
 * Class which implements Undo/Redo logic.
 * Uses an event sourcing approach.
 * Make sure that no modifications are made beside the {@link UndoManager#apply(Modification)}
 * or {@link UndoManager#applyWithoutHistory(Modification)} method!
 *
 * @param <A> the structure to modify
 */
public class UndoManager<A extends Copyable<A>> {
    private ArrayList<ChangedListener> listeners;
    private ArrayList<Modification<A>> modifications;
    private int modificationCounter;
    private int savedCounter;
    private A initial;
    private A actual;

    /**
     * Creates anew instance
     *
     * @param initial the initial structure
     */
    public UndoManager(A initial) {
        listeners = new ArrayList<>();
        setInitial(initial);
    }

    /**
     * Sets the initial instance.
     * The history is lost
     *
     * @param initial the initial state
     */
    public void setInitial(A initial) {
        this.initial = initial;
        actual = null;
        modifications = new ArrayList<>();
        modificationCounter = 0;
        savedCounter = 0;
        fireChangedEvent();
    }


    /**
     * Applies a modification
     *
     * @param modification the modification to apply
     * @throws ModifyException ModifyException
     */
    public void apply(Modification<A> modification) throws ModifyException {
        try {
            if (actual == null)
                actual = initial.createDeepCopy();
            modification.modify(actual);
            while (modificationCounter < modifications.size())
                modifications.remove(modifications.size() - 1);
            modifications.add(modification);
            modificationCounter = modifications.size();
            fireChangedEvent();
        } catch (ModifyException e) {
            throw createTrace(e, null);
        }
    }

    private void fireChangedEvent() {
        for (ChangedListener l : listeners)
            l.hasChanged();
    }

    /**
     * Does a redo if possible
     *
     * @throws ModifyException ModifyException
     */
    public void redo() throws ModifyException {
        if (redoAvailable()) {
            try {
                modifications.get(modificationCounter).modify(actual);
                modificationCounter++;
                fireChangedEvent();
            } catch (ModifyException e) {
                throw createTrace(e, null);
            }
        }
    }

    /**
     * @return true if redo is possible
     */
    public boolean redoAvailable() {
        return modificationCounter < modifications.size();
    }

    /**
     * Does a undo if possible
     *
     * @throws ModifyException ModifyException
     */
    public void undo() throws ModifyException {
        if (undoAvailable()) {
            Modification<A> lastWorkingModification = null;
            try {
                A newActual = initial.createDeepCopy();
                for (int i = 0; i < modificationCounter - 1; i++) {
                    Modification<A> m = modifications.get(i);
                    m.modify(newActual);
                    lastWorkingModification = m;
                }
                modificationCounter--;
                actual = newActual;
                fireChangedEvent();
            } catch (ModifyException e) {
                throw createTrace(e, lastWorkingModification);
            }
        }
    }

    private ModifyException createTrace(ModifyException cause, Modification<A> lastWorkingModification) {
        StringBuilder sb = new StringBuilder("Exception during event processing");
        for (int i = 0; i < modifications.size(); i++) {
            if (i == modificationCounter)
                sb.append("\n>");
            else
                sb.append("\n ");
            Modification<A> m = modifications.get(i);
            sb.append(m.toString());
            if (m == lastWorkingModification)
                sb.append("\n -> exception in the following modification!");
        }
        if (modificationCounter == modifications.size())
            sb.append("\n>");
        return new ModifyException(sb.toString(), cause);
    }

    /**
     * @return the modification which is reverted
     */
    public Modification<A> getUndoModification() {
        if (undoAvailable())
            return modifications.get(modificationCounter - 1);
        else
            return null;
    }

    /**
     * @return the modification which is applied again
     */
    public Modification<A> getRedoModification() {
        if (redoAvailable())
            return modifications.get(modificationCounter);
        else
            return null;
    }

    /**
     * @return true if undo is possible
     */
    public boolean undoAvailable() {
        return modificationCounter > 0;
    }

    /**
     * Marks the actual state a s saved
     */
    public void saved() {
        savedCounter = modificationCounter;
    }

    /**
     * @return true if object is modified
     */
    public boolean isModified() {
        return savedCounter != modificationCounter;
    }

    /**
     * @return the actual state
     */
    public A getActual() {
        if (actual == null)
            return initial;
        else
            return actual;
    }

    /**
     * Adds a listener
     *
     * @param listener the listener to add
     * @return the given listener for chained calls.
     */
    public ChangedListener addListener(ChangedListener listener) {
        listeners.add(listener);
        return listener;
    }

    /**
     * Removed a listener.
     *
     * @param listener the listener to remove
     */
    public void removedListener(ChangedListener listener) {
        listeners.remove(listener);
    }


    /**
     * Applies a modification to the object without a history record.
     * Needs to be used to ensure all existing copies are modified.
     *
     * @param modification the modification
     * @throws ModifyException ModifyException
     */
    public void applyWithoutHistory(Modification<A> modification) throws ModifyException {
        if (actual != null)
            modification.modify(actual);
        modification.modify(initial);
    }

}
