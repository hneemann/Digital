/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

/**
 * Interface to identify a Fragment which may habe a Pin inside
 * 
 * @author felix
 */
public interface SVGPinnable {
	/**
	 * Gets the Pins
	 * 
	 * @return pins
	 */
	SVGPseudoPin[] getPin();
}
