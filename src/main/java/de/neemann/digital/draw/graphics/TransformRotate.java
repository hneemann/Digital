package de.neemann.digital.draw.graphics;

/**
 * Implements a rotation and translation.
 *
 * @author hneemann
 */
public class TransformRotate implements Transform {

    private final int sin;
    private final int cos;
    private final Vector translation;

    /**
     * Creates a new instance
     *
     * @param translation the translation
     * @param rot         the rotation
     */
    public TransformRotate(Vector translation, int rot) {
        this.translation = translation;
        switch (rot) {
            case 1:
                sin = 1;
                cos = 0;
                break;
            case 2:
                sin = 0;
                cos = -1;
                break;
            case 3:
                sin = -1;
                cos = 0;
                break;
            default:// 0
                sin = 0;
                cos = 1;
                break;
        }
    }

    @Override
    public Vector transform(Vector v) {
        return new Vector(v.x * cos + v.y * sin, -v.x * sin + v.y * cos).add(translation);
    }
}
