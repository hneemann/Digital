package de.neemann.digital.draw.graphics;

/**
 * @author hneemann
 */
public enum Orientation {
    LEFTBOTTOM(0, 0),
    CENTERBOTTOM(1, 0),
    RIGHTBOTTOM(2, 0),
    RIGHTCENTER(2, 1),
    RIGHTTOP(2, 2),
    CENTERTOP(1, 2),
    LEFTTOP(0, 2),
    LEFTCENTER(0, 1),
    CENTERCENTER(1, 1);


    private final int x;
    private final int y;

    Orientation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x orientation value 0,1,2
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y orientation value 0,1,2
     */
    public int getY() {
        return y;
    }

    /**
     * ROTATE this orientation by the given angle.
     *
     * @param n the angle in units of 45 degrees
     * @return the new orientation
     */
    public Orientation rot(int n) {
        if (this == CENTERCENTER) return CENTERCENTER;

        int p = this.ordinal() + n * 2;
        if (p > 7) p = p - 8;
        if (p < 0) p = p + 8;
        return Orientation.values()[p];
    }

}
