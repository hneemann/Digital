/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.rt;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author ideras
 */
public class ArrayValue extends RtValue implements IStruct, Iterable<RtReference> {

    private final ArrayList<RtReference> elements;

    /**
     * Creates a new array of the specified size.
     * @param size the size
     */
    public ArrayValue(int size) {
        elements = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            elements.add(new RtReference());
        }
    }

    /**
     * Creates a new array instance.
     */
    public ArrayValue() {
        elements = new ArrayList<>();
    }

    /**
     * Returns the element reference at the specified index.
     *
     * @param index the index.
     * @return the element reference or null if no such index exist.
     */
    public RtReference getReferenceAt(int index) {
        if (index >= 0 && index < elements.size()) {
            return elements.get(index);
        } else {
            return null;
        }
    }

        /**
     * Returns the element value at the specified index.
     *
     * @param index the index.
     * @return the element value or null if no such index exist.
     */
    public RtValue get(int index) {
        if (index >= 0 && index < elements.size()) {
            return elements.get(index).getTarget();
        } else {
            return null;
        }
    }

     /**
     * Checks if the array size is less or equal to the specified, if not it grows the array.
     *
     * @param newSize the new size.
     */
    public void ensureCapacity(int newSize) {
        if (elements.size() < newSize) {
            for (int i = elements.size(); i < newSize; i++) {
                elements.add(new RtReference());
            }
        }
    }

    /**
     * Set the value of element at the specified index.
     *
     * @param index the index
     * @param value the value
     */
    public void set(int index, RtValue value) {
        RtReference refv = elements.get(index);
        refv.set(value);
    }

    /**
     * Return the array size.
     *
     * @return the size
     */
    public int getSize() {
        return elements.size();
    }

    @Override
    public RtValue getFieldValue(String fieldName) {
        if (fieldName.equals("size")) {
            return new IntValue(elements.size());
        } else {
            return null;
        }
    }

    @Override
    public RtReference getFieldReference(String fieldName) {
        if (fieldName.equals("size")) {
            return new RtReference(new IntValue(elements.size()));
        } else {
            return null;
        }
    }

    @Override
    public void setFieldValue(String fieldName, RtValue value) {
        throw new RuntimeException("Cannot set field values on array");
    }

    @Override
    public Type getType() {
        return Type.ARRAY;
    }

    @Override
    public Iterator<RtReference> iterator() {
        return elements.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        sb.append("(");
        for (RtReference vref : elements) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(vref.getTarget().toString());
        }
        sb.append(")");

        return sb.toString();
    }

}
