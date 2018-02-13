package de.neemann.digital.draw.graphics.svg;

/**
 * Interface of all representations of SVG-Elements
 * @author felix
 */
public interface SVGFragment {

    /**
     * Get Drawable representations of the elements
     * @return Array of Drawable Objects
     */
    Drawable[] getDrawables();
}
