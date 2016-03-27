package de.neemann.digital.core.element;

/**
 * @author hneemann
 */
public class Rotation {
    public int rotation;

    public Rotation(int rot) {
        rotation = rot;
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
