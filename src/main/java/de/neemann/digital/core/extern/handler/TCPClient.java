/*
 * Copyright (c) 2024 Ron Ren.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern.handler;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * TCP CLIENT for extern
 */
public class TCPClient {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;

    private BlockingQueue<byte[]> sendQueue = new LinkedBlockingQueue<byte[]>();
    private volatile byte[] recvMsg;

    private volatile boolean isRunning = false;

    /**
     * status
     * @return current status
     */
    public boolean getIsRunning() {
        return this.isRunning;
    }

    /***
     * construction
     * @param serverAddress  extern app ip address
     * @param serverPort extern app port
     */
    public TCPClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * start tcpclient
     * @throws IOException exception
     */
    public void start() throws IOException {
        if (this.socket == null || this.socket.isClosed() || !this.isRunning) {
            socket = new Socket(serverAddress, serverPort);
            this.output = new DataOutputStream(socket.getOutputStream());
            this.input = new DataInputStream(socket.getInputStream());
            this.isRunning = true;
            new Thread(new Sender(this)).start();
            new Thread(new Receiver(this)).start();
        }
    }

    /***
     * send message to extern app
     * @param bytes  send bytes
     * @throws InterruptedException put msg failed.
     */
    public void sendMessage(byte[] bytes) throws InterruptedException {
        sendQueue.put(bytes);
    }

    /***
     * get message from extern app
     * @return current extern app signals
     */
    public byte[] receiveMessage() {
        return recvMsg;
    }

    /***
     * sender
     */
    private class Sender implements Runnable {
        private TCPClient tcpClient;

        /**
         * construction
         * @param tcpClient  tcpclient instance
         */
        Sender(TCPClient tcpClient) {
            this.tcpClient = tcpClient;
        }
        @Override
        public void run() {
            try {
                while (this.tcpClient.isRunning) {
                    byte[] msg = sendQueue.take();
                    this.tcpClient.output.writeInt(msg.length);
                    this.tcpClient.output.write(msg);
                    this.tcpClient.output.flush();
                }
            } catch (InterruptedException e) {
                System.out.println("Sender interrupted.");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    this.tcpClient.output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.tcpClient.isRunning = false;
            }
        }
    }

    /***
     * Receiver
     */
    private class Receiver implements Runnable {
        private TCPClient tcpClient;
        /**
         * construction
         * @param tcpClient  tcpclient instance
         */
        Receiver(TCPClient tcpClient) {
            this.tcpClient = tcpClient;
        }
        @Override
        public void run() {
            try {
                while (this.tcpClient.isRunning) {
                    int msgLen = this.tcpClient.input.readInt();
                    this.tcpClient.recvMsg = new byte[msgLen];
                    this.tcpClient.input.readFully(this.tcpClient.recvMsg);
                }
            } catch (IOException e) {
                System.out.println("Receiver interrupted.");
            } finally {
                try {
                    this.tcpClient.input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.tcpClient.isRunning = false;
            }
        }
    }

    /**
     * close tcp connection
     * @throws IOException exception
     */
    public void close() throws IOException {
        this.isRunning = false;
        socket.close();
    }
}
