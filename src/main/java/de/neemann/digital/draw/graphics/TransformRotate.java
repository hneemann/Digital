package de.neemann.digital.draw.graphics;

/**
 * @author hneemann
 */
public class TransformRotate implements Transform {

    private final int sin;
    private final int cos;
    private final Vector pos;

    public TransformRotate(Vector pos, int rot) {
        this.pos = pos;
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
        return new Vector(v.x * cos + v.y * sin, -v.x * sin + v.y * cos).add(pos);
    }
}
