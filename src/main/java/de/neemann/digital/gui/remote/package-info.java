/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */

/**
 * Classes to implement a remote server for simulator control.
 * Is used to allow a assembly IDE to control the simulator.
 * In this way the assembly IDE is able to load executables to the program ROM and
 * start the simulation (resets the processor). After that you can start single clock steps,
 * run to break and so on.
 * Every command returns the actual ROM address to the assembly IDE which can be used to highlight
 * the actual executed assembly instruction within the IDE.
 * <p/>
 * Created by helmut.neemann on 23.06.2016.
 */
package de.neemann.digital.gui.remote;
