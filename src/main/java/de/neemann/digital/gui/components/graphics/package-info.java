/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */

/**
 * Implements a simple graphics card
 * The graphics card behaves like a single port RAM. You can set the size of the graphics in pixels.
 * Every pixel is represented by one byte in the graphics RAM.
 * The RAM is twice als large as needed. So two banks are supported to allow double buffering.
 * The bank, which is shown is selected by an additional input bit.
 */
package de.neemann.digital.gui.components.graphics;
