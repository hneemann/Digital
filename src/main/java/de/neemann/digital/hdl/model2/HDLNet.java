/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import java.util.ArrayList;

public class HDLNet {
    private String name;
    private ArrayList<HDLPort> inputs;
    private HDLPort output;

    public HDLNet(String name) {
        this.name = name;
        inputs = new ArrayList<>();
    }

    public void addPort(HDLPort hdlPort) throws HDLException {
        if (hdlPort.getDirection().equals(HDLPort.Direction.OUT)) {
            if (output != null)
                throw new HDLException("multiple outputs connected to net " + name + ": " + output + " and " + hdlPort);
            output = hdlPort;
        } else
            inputs.add(hdlPort);
    }

    @Override
    public String toString() {
        return name + " (" + output + " " + inputs + ")";
    }

    public void fixBits() throws HDLException {
        if (output == null)
            throw new HDLException("no output connected to net");
        final int bits = output.getBits();
        if (bits == 0)
            throw new HDLException("no bit number set for output " + output.getName());

        for (HDLPort i : inputs)
            i.setBits(bits);

    }

    public void remove(HDLPort p) {
        inputs.remove(p);
    }
}
