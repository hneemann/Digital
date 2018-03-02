/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
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
