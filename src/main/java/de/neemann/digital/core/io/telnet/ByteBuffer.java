/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io.telnet;

/**
 * A simple thread save byte queue.
 */
public class ByteBuffer {
    private final byte[] data;
    private final int size;
    private int inBuffer;
    private int newest;
    private int oldest;

    /**
     * Creates a new instance
     *
     * @param size the size of the buffer
     */
    public ByteBuffer(int size) {
        data = new byte[size];
        this.size = size;
    }

    /**
     * Adds a byte at the top of the buffer
     *
     * @param value the byte value
     */
    synchronized public void put(byte value) {
        if (inBuffer < size) {
            data[newest] = value;
            newest = inc(newest);
            inBuffer++;
        }
    }

    /**
     * @return the byte at the tail of the buffer
     */
    synchronized public byte peek() {
        if (inBuffer > 0) {
            return data[oldest];
        } else
            return -1;
    }

    /**
     * deletes a byte from the tail of the buffer
     */
    synchronized public void delete() {
        if (inBuffer > 0) {
            oldest = inc(oldest);
            inBuffer--;
        }
    }

    /**
     * deletes all buffered data
     */
    synchronized public void deleteAll() {
        oldest = 0;
        newest = 0;
        inBuffer = 0;
    }

    /**
     * @return true if there is data available
     */
    synchronized public boolean hasData() {
        return inBuffer > 0;
    }

    private int inc(int n) {
        n++;
        if (n >= size)
            n = 0;
        return n;
    }

}
