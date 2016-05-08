package de.neemann.digital.draw.builder;

/**
 * @author hneemann
 */
public class Box {

    private final int width;
    private final int height;

    public Box(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
