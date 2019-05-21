/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.tt2;

import java.io.OutputStream;
import java.util.Arrays;

/**
 * Implements a ByteArrayOutputStream which stores only the last [size] bytes.
 */
public class RotationByteArrayOutputStream extends OutputStream {
    private final byte[] buffer;
    private final int size;
    private int inBuffer;
    private int pos;
    private int skipped;

    /**
     * Creates a new instance
     *
     * @param size the size of the buffer
     */
    public RotationByteArrayOutputStream(int size) {
        buffer = new byte[size];
        this.size = size;
    }

    @Override
    public void write(int i) {
        buffer[pos] = (byte) i;
        pos++;
        if (pos == size) {
            pos = 0;
        }
        if (inBuffer < size) {
            inBuffer++;
        } else {
            skipped++;
        }
    }

    /**
     * Returns a byte array containing the data
     *
     * @return the byte array
     */
    public byte[] toByteArray() {
        if (inBuffer < size) {
            return Arrays.copyOf(buffer, inBuffer);
        } else {
            byte[] ret = new byte[size];
            System.arraycopy(buffer, pos, ret, 0, size - pos);
            System.arraycopy(buffer, 0, ret, size - pos, pos);
            return ret;
        }
    }

    /**
     * @return the number of skipped bytes
     */
    public int getSkipped() {
        return skipped;
    }

    @Override
    public String toString() {
        if (skipped > 0) {
            return "skipped bytes: " + skipped + "\n" + new String(toByteArray());
        }
        return new String(toByteArray());
    }
}
