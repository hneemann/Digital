package de.neemann.digital.gui.remote;

/**
 * Interface to implement a simple server.
 * <p/>
 * Created by helmut.neemann on 23.06.2016.
 */
public interface HandlerInterface {
    /**
     * Handles a simple string request
     *
     * @param request the request
     * @return the response
     */
    String handleRequest(String request);
}
