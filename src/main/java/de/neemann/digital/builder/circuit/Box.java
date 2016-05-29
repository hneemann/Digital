package de.neemann.digital.builder.circuit;

/**
 * Width and height of a fragment
 *
 * @author hneemann
 */
public class Box {

    private final int width;
    private final int height;

    /**
     * Create a new instance
     *
     * @param width  width
     * @param height height
     */
    public Box(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return height
     */
    public int getHeight() {
        return height;
    }
}
