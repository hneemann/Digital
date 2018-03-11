/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Simple server to remote control the simulator.
 * Every incoming request is passed to the {@link HandlerInterface}.
 * The returned string is then returned to the client as response.
 * <p/>
 * Created by helmut.neemann on 23.06.2016.
 */
public class RemoteSever implements Runnable {
    private final HandlerInterface handler;
    private ServerSocket socket;
    private Thread thread;

    /**
     * Creates a new server instance
     *
     * @param handler the handler interface to handle simple string requests
     */
    public RemoteSever(HandlerInterface handler) {
        this.handler = handler;
    }

    /**
     * Stars the server
     * @param port the port
     * @throws IOException IOException
     */
    public void start(int port) throws IOException {
        socket = new ServerSocket(port);
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            while (!thread.isInterrupted()) {
                try (Socket s = socket.accept()) {
                    DataInputStream in = new DataInputStream(s.getInputStream());
                    String request = in.readUTF();
                    String response = handler.handleRequest(request);
                    DataOutputStream out = new DataOutputStream(s.getOutputStream());
                    out.writeUTF(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
