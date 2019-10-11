/*
 * Copyright (c) 2019 Helmut Neemann & Mats Engstrom.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;
import java.io.DataOutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
//import java.util.Iterator;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Port component.
 */
public class Port extends Node implements Element {

    static private Deque<Integer> buf = new ArrayDeque<Integer>(1000);
    static private PortSocket portSocket = null;

    /**
     * The Port description
     */

    public static final ElementTypeDescription DESCRIPTION
        = new ElementTypeDescription(Port.class) {
        public PinDescriptions getInputDescription(ElementAttributes elementAttributes) throws NodeException {
            if (elementAttributes.get(Keys.PORT_MODE))
                return new PinDescriptions(
                    input("in"),
                    input("C").setClock());
            else
                return new PinDescriptions(
                    input("D"),
                    input("C").setClock(),
                    input("stb"),
                    input("ack"));
        }
    }
    .addAttribute(Keys.ROTATE)
    .addAttribute(Keys.LABEL)
    .addAttribute(Keys.INVERTER_CONFIG)
    .addAttribute(Keys.PORT_MODE)
    .addAttribute(Keys.PORT_TELNET);

    private boolean portMode; // false=parallel, true=serial
    private boolean portTelnet; // false=raw, true=telnet

    private ObservableValue clock;
    private ObservableValue sin;    // Serial input
    private ObservableValue sout;   // Serial output
    private ObservableValue pin;    // Parallel input (D)
    private ObservableValue pout;   // Parallel output (Q)
    private ObservableValue stb;    // Strobe for parallel input data
    private ObservableValue bsy;    // Busy processing parallel input data
    private ObservableValue avail;  // Parallel output data available at Q
    private ObservableValue ack;    // Acknowledge that data is read from Q

    private boolean lastClock = false;
    private int bsyCnt = 0;
    private long pinVal = 0;
    private long poutVal = 0;
    private boolean lastStb = false;
    private boolean  lastAck = false;
    private String label;

    /**
     * Creates a new instance
     *
     * @param attributes the elements attributes
     */
    public Port(ElementAttributes attributes) {
        portMode = attributes.get(Keys.PORT_MODE);
        portTelnet = attributes.get(Keys.PORT_TELNET);
        label = attributes.getLabel();

        if (portMode) {
            sout = new ObservableValue("out", 1).setPinDescription(DESCRIPTION);
        } else {
            pout = new ObservableValue("Q", 8).setPinDescription(DESCRIPTION);
            bsy = new ObservableValue("bsy", 1).setPinDescription(DESCRIPTION);
            avail = new ObservableValue("avail", 1).setPinDescription(DESCRIPTION);
        }
        if (portSocket == null) {
            portSocket = new PortSocket(this, 2323, portTelnet);
            portSocket.start();
        }
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        if (portMode) {
            sin = inputs.get(0).addObserverToValue(this).checkBits(1, this, 0);
            clock = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
        } else {
            pin = inputs.get(0).addObserverToValue(this).checkBits(8, this, 0);
            clock = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
            stb = inputs.get(2).addObserverToValue(this).checkBits(1, this, 2);
            ack = inputs.get(3).addObserverToValue(this).checkBits(1, this, 3);
        }
    }

    @Override
    public ObservableValues getOutputs() {
        if (portMode) {
            return new ObservableValues(sout);
        } else {
            return new ObservableValues(pout, bsy, avail);
        }
    }


    @Override
    public void readInputs() throws NodeException {
        boolean nowClock = clock.getBool();

        if (!portMode) {
            boolean nowStb = stb.getBool();
            boolean nowAck = ack.getBool();

            // If rising edge on stb then send the paralell-in (pin) to
            // the socket, and set the bsy flag (it will be automatically
            // be cleared in X clock cycles to simulate a slow external device)
            if (lastStb != nowStb) {
                if (nowStb) {
                    if (!bsy.getBool()) {
                        toSocket((int) pin.getValue());
                        bsy.setValue(1);
                        bsyCnt=100;
                    }
                }
                lastStb=nowStb;
            }

            // Check if it's time to clear the bsy flag
            if (!portMode) {
                if (lastClock != nowClock) {
                    if (bsyCnt>0) {
                        if (--bsyCnt==0) bsy.setValue(0);
                    }
                }
            }

            // If there's pending data in the buffer and the last
            // data has been ack'ed then update the Q port
            if (!buf.isEmpty() && !avail.getBool()) {
                poutVal=buf.remove();
                pout.set((long) poutVal, 0);
                avail.setValue(1);
            }

            // If rising edge on ack then clear the avail flag
            if (lastAck != nowAck) {
                if (nowAck) avail.setValue(0);
                lastAck=nowAck;
            }

            lastClock = nowClock;
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (!portMode) {
            pout.setValue(poutVal);
        }
    }

    /**
     * Clear the buffer when the simulation is started
     *
     */
    @Override
    public void init(Model model) throws NodeException {
        buf.clear();
    }

    /**
     * The PortSocket thread calls here when new data has
     * arrived from the network socket.
     *
     * @param v - data received from socket
     */
    public void fromSocket(int v) {
        buf.add(v);
    }

    /**
     * Send a byte to the network socket
     *
     * @param v - data to send to socket
     */
    public void toSocket(int v) {
        DataOutputStream os=portSocket.getOutstream();
        try {
            os.writeByte(v);
        } catch(Exception e) {
        }
    }

}
