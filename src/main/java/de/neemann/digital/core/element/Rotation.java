/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.element;

/**
 * Wrapper for the elements rotation
 */
public class Rotation {
    /**
     * The rotation value.
     * is in between 0 and 3
     */
    private final int rotation;

    /**
     * Creates a new instance
     *
     * @param rotation the rotation
     */
    public Rotation(int rotation) {
        this.rotation = rotation;
    }

    /**
     * @return the rotation value
     */
    public int getRotation() {
        return rotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rotation rotation1 = (Rotation) o;

        return rotation == rotation1.rotation;
    }

    @Override
    public int hashCode() {
        return rotation;
    }
}
