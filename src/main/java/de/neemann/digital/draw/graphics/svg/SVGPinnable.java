package de.neemann.digital.draw.graphics.svg;

/**
 * Interface to identify a Fragment which may habe a Pin inside
 * @author felix
 */
public interface SVGPinnable {
    /**
     * Gets the Pins
     * @return pins
     */
    SVGPseudoPin[] getPin();
}
