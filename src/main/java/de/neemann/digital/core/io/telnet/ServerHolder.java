/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io.telnet;

import java.io.IOException;
import java.util.HashMap;

/**
 * Simple singleton to hold the server instances.
 * Usage of this singleton allows the telnet client to stay connected
 * also if the simulation is not running.
 */
public final class ServerHolder {
    /**
     * The singleton instance
     */
    public static final ServerHolder INSTANCE = new ServerHolder();

    private final HashMap<Integer, Server> serverMap;

    private ServerHolder() {
        serverMap = new HashMap<>();
    }

    /**
     * Returns a server.
     *
     * @param port the port
     * @return the server
     * @throws IOException IOException
     */
    public Server getServer(int port) throws IOException {
        Server server = serverMap.get(port);
        if (server == null || server.isDead()) {
            server = new Server(port);
            serverMap.put(port, server);
        } else
            server.deleteAll();
        return server;
    }
}
