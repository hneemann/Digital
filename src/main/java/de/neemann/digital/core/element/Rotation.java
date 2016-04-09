package de.neemann.digital.core.element;

/**
 * Warapper for the elements rotation
 *
 * @author hneemann
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
