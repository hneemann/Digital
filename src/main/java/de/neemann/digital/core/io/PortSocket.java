/*
 * Copyright (c) 2019 Helmut Neemann & Mats Engstrom.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
//import java.net.SocketTimeoutException;
//import java.util.concurrent.TimeoutException;

/**
 *
 */
public class PortSocket extends Thread {



    private Port port;
    private boolean telnetMode;
    private ServerSocket serverSocket;
    private DataOutputStream outStream;

    /**
     * @param port instance of creator
     * @param listenPort - TCP port to liste to
     * @param telnetMode - True if a telnet terminal will connect
     */
    public PortSocket(Port port, int listenPort, boolean telnetMode) {
        this.port = port;
        this.telnetMode = telnetMode;
        try {
            serverSocket = new ServerSocket(listenPort);
        } catch (IOException e) {
            System.out.println("Error opening port " + listenPort + " : " + e.toString());
        }
    }

    /**
     *
     */
    @Override
    public void run() {
        Socket lastClient = null;
        while (true) {
            try {
                Socket client = serverSocket.accept();
                if (lastClient != null) {
                    lastClient.close();
                }
                lastClient = client;

                ClientThread ct = new ClientThread(client);
                ct.start();
            } catch (Exception e) {
                System.out.println("Server exception: " + e.toString());
            }
        }
    }

    /**
     *
     */
    class ClientThread extends Thread {
        private Socket cli;

        /**
         *
         */
        ClientThread(Socket client) {
            cli = client;
        }

        /**
         *
         */
        public void run() {
            byte[] buffer = new byte[1024];
            int read;
            try {
                DataInputStream inStream = new DataInputStream(cli.getInputStream());
                outStream = new DataOutputStream(cli.getOutputStream());

                // If user wants to connect with telnet then we need to
                // turn off local each and line buffering in the telnet
                // terminal
                if (telnetMode) {
                    outStream.writeBytes("\377\373\003"); // send IAC WILL SUPPRESS-GOAHEAD
                    outStream.writeBytes("\377\375\003"); // send IAC DO SUPPRESS-GO-AHEAD
                    outStream.writeBytes("\377\373\001"); // send IAC WILL SUPPRESS-ECHO
                    outStream.writeBytes("\377\375\001"); // send IAC DO SUPPRESS-ECHO
                    outStream.flush();
                    Thread.sleep(100); // Wait for a bit and eat up all replies from telnet
                    inStream.read(buffer);
                    outStream.writeChars("[Digital UART]\r\n");
                }

                while (true) {
                    while ((read = inStream.read(buffer)) != -1) {
                        int lastChar = 0;
                        for (int i = 0; i < read; i++) {
                            int c = buffer[i];
                            if (telnetMode && lastChar == 13 && c == 0) {
                                c=0; // Ignore character (keep linter happy)
                            } else {
                                port.fromSocket(c);
                            }
                            lastChar = c;
                        }
                    }
                    inStream.close();
                    outStream.close();
                    break;
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    /**
     * @return the current socket output stream
     */
    public DataOutputStream getOutstream() {
        return outStream;
    }

}
