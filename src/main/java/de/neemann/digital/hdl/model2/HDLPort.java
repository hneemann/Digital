/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

public class HDLPort {

    public enum Direction {
        IN, OUT
    }

    private final String name;
    private final Direction direction;
    private int bits;
    private HDLNet net;
    private HDLNode node;
    private String pinNumber;

    public HDLPort(String name, HDLNet net, Direction direction, int bits) throws HDLException {
        this.name = name;
        this.net = net;
        this.direction = direction;
        this.bits = bits;

        if (net != null)
            net.addPort(this);
    }

    public HDLPort setPinNumber(String pinNumber) {
        this.pinNumber = pinNumber;
        return this;
    }

    public HDLNet getNet() {
        return net;
    }

    public void setNet(HDLNet net) throws HDLException {
        this.net = net;
        net.addPort(this);
    }

    public String getName() {
        return name;
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

    public Direction getDirection() {
        return direction;
    }

    public HDLNode getNode() {
        return node;
    }

    public void setNode(HDLNode node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return direction + " " + name + "(" + bits + ")";
    }

}
