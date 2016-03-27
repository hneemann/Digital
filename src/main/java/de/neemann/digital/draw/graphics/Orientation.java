package de.neemann.digital.draw.graphics;

/**
 * @author hneemann
 */
public enum Orientation {
    LEFTCENTER(0, 1),
    RIGHTCENTER(2, 1),
    CENTERCENTER(1, 1),

    LEFTTOP(0, 2),
    RIGHTTOP(2, 2),
    CENTERTOP(1, 2),

    LEFTBOTTOM(0, 0),
    RIGHTBOTTOM(2, 0),
    CENTERBOTTOM(1, 0);

    private final int x;
    private final int y;

    Orientation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
