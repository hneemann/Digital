/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.gui.remote.RemoteException;

import java.io.File;

/**
 * Interface which is used by the remote server
 * <p>
 * Created by helmut.neemann on 23.06.2016.
 */
public interface DigitalRemoteInterface {

    /**
     * Starts the model
     *
     * @param file      the file to load to program rom
     * @param bigEndian use bigEndian at import
     * @throws RemoteException RemoteException
     */
    void start(File file, boolean bigEndian) throws RemoteException;

    /**
     * Starts the model in debug mode
     *
     * @param file      the file to load to program rom
     * @param bigEndian use bigEndian at import
     * @throws RemoteException RemoteException
     */
    void debug(File file, boolean bigEndian) throws RemoteException;

    /**
     * performs a single step
     *
     * @return actual position
     * @throws RemoteException RemoteException
     */
    String doSingleStep() throws RemoteException;

    /**
     * changes the clock
     *
     * @return actual position
     * @throws RemoteException RemoteException
     */
    String doClock() throws RemoteException;

    /**
     * runs model to the next BRK instruction
     *
     * @return actual position
     * @throws RemoteException RemoteException
     */
    String runToBreak() throws RemoteException;

    /**
     * stops the model
     *
     * @throws RemoteException RemoteException
     */
    void stop() throws RemoteException;

    /**
     * Used to obtain the models state
     *
     * @return a JSON string containing the measurement values
     * @throws RemoteException RemoteException
     */
    String measure() throws RemoteException;
}
