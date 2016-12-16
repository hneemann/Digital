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
     * Loads the given file to the data rom
     *
     * @param file the file to load
     * @throws RemoteException RemoteException
     */
    void loadRom(File file) throws RemoteException;

    /**
     * Starts the model
     *
     * @throws RemoteException RemoteException
     */
    void start() throws RemoteException;

    /**
     * Starts the model in debug mode
     *
     * @throws RemoteException RemoteException
     */
    void debug() throws RemoteException;

    /**
     * performs a single step
     *
     * @return actual position
     * @throws RemoteException RemoteException
     */
    String doSingleStep() throws RemoteException;

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
}
