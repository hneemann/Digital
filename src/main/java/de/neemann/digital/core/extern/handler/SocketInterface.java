/*
 * Copyright (c) 2024 Ron Ren.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern.handler;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.extern.Port;
import de.neemann.digital.core.extern.PortDefinition;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * signal comm out from extern app with shared mem map file
 */
public class SocketInterface implements ProcessInterface {

    private TCPClient tcpClient;

    private int inputBits;
    private int outputBits;
    private BitSet inSeta;
    private BitSet inSetb;
    private int inLen;
    private BitSet outSet;
    private ByteBuffer buf1;
    private ByteBuffer buf2;


    /**
     * mem map init
     * @param ipPort  address and port for extern
     * @param inputs signal inputs
     * @param outputs  signal outputs
     */
    public SocketInterface(String ipPort, PortDefinition inputs, PortDefinition outputs) {
        String[] net = ipPort.split(":");
        this.tcpClient = new TCPClient(net[0], Integer.parseInt(net[1]));
        this.inputBits = 0;
        this.outputBits = 0;
        for (Port input : inputs) {
            this.inputBits += input.getBits();
            this.inLen = this.inputBits;
            // match the VPI t_vpi_value type,t_vpi_vecval use aval and bval with int32
            while (this.inLen % 32 != 0) {
                this.inLen++;
            }
            this.inSeta = new BitSet(this.inLen);
            this.inSetb = new BitSet(this.inLen);
            this.buf1 = ByteBuffer.allocate(this.inLen/8);
            this.buf2 = ByteBuffer.allocate(this.inLen/8);
        }
        for (Port output : outputs) {
            this.outputBits += output.getBits();
        }
    }

    private void initSocket() throws IOException {
        if (!this.tcpClient.getIsRunning()) {
            this.tcpClient.start();
        }
    }

    private static byte[] concatenateByteArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length]; // 新数组，长度为两个数组的总和

        System.arraycopy(array1, 0, result, 0, array1.length); // 将array1复制到result
        System.arraycopy(array2, 0, result, array1.length, array2.length); // 将array2复制到result

        return result;
    }

    @Override
    public void writeValues(ObservableValues values) throws IOException {
        this.initSocket();
        int pos = 0;
        for (ObservableValue v : values) {
            for (int i=0; i<v.getBits(); i++) {
                long mask =  (1L << i);
                long value = v.getValue() & mask;
                long hZ = v.getHighZ() & mask;
                /* ab encoding: 00=0, 10=1, 11=X, 01=Z */
                if (value > 0) {
                    this.inSeta.set(pos, true);
                    this.inSetb.set(pos, false);
                } else if (hZ > 0) {
                    this.inSeta.set(pos, false);
                    this.inSetb.set(pos, true);
                } else {
                    this.inSeta.set(pos, false);
                    this.inSetb.set(pos, false);
                }
                pos++;
            }
        }
        this.buf1.clear();
        this.buf2.clear();
        this.buf1.put(this.inSeta.toByteArray());
        this.buf2.put(this.inSetb.toByteArray());
        while (this.buf1.position() < this.inLen/8) {
            this.buf1.put((byte) 0);
        }
        while (this.buf2.position() < this.inLen/8) {
            this.buf2.put((byte) 0);
        }
        try {
            this.tcpClient.sendMessage(SocketInterface.concatenateByteArrays(this.buf1.array(), this.buf2.array()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void readValues(ObservableValues values) throws IOException {
        this.initSocket();
        byte[] buf = this.tcpClient.receiveMessage();
        if (buf == null) {
            return;
        }
        int posAdd = (buf.length*8)/2;
        this.outSet = BitSet.valueOf(buf);
        int pos = 0;
        for (ObservableValue v : values) {
            long value = 0L;
            long hZ = 0L;
            for (int i=0; i<v.getBits(); i++) {
                long mask =  (1L << i);
                /* ab encoding: 00=0, 10=1, 11=X, 01=Z */
                boolean l = this.outSet.get(pos);
                boolean r = this.outSet.get(pos + posAdd);
                if (l && !r) {
                    value = value | mask;
                } else if (!l && r) {
                    hZ = hZ | mask;
                }
                pos++;
            }
            v.set(value, hZ);
        }
    }

    @Override
    public void close() throws IOException {
        this.tcpClient.close();
    }
}
