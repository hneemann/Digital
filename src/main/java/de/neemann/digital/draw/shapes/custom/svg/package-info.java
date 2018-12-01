/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */

/**
 * Simple importer for svg files.
 * A pin is specified by a circle which has an id of the form "pin:[name]" or "pin+:[name]".
 * The later one enables the pin label in the shape.
 * In this case the circle itself is ignored.
 */
package de.neemann.digital.draw.shapes.custom.svg;
