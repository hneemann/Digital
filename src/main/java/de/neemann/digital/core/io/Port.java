/*
 * Copyright (c) 2019 Helmut Neemann & Mats Engstrom.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import java.io.DataOutputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Port component.
 */
public class Port extends Node implements Element {

    /**
     * The Port description
     */
    public static final ElementTypeDescription DESCRIPTION
        = new ElementTypeDescription(Port.class) {
        public PinDescriptions getInputDescription(ElementAttributes elementAttributes) throws NodeException {
            if (elementAttributes.get(Keys.PORT_MODE)==PortMode.serial)
                return new PinDescriptions(
                    // Serial
                    input("in"),
                    input("C").setClock()).setLangKey(getPinLangKey());
            else
                return new PinDescriptions(
                    // Parallel
                    input("D"),
                    input("C").setClock(),
                    input("stb"),
                    input("ack")).setLangKey(getPinLangKey());
        }
    }
    .addAttribute(Keys.ROTATE)
    .addAttribute(Keys.LABEL)
    .addAttribute(Keys.INVERTER_CONFIG)
    .addAttribute(Keys.PORT_MODE)
    .addAttribute(Keys.PORT_TELNET);

    private final String label;
    private ObservableValue clock;
    private ObservableValue sin;    // Serial input
    private ObservableValue sout;   // Serial output
    private ObservableValue pin;    // Parallel input (D)
    private ObservableValue pout;   // Parallel output (Q)
    private ObservableValue stb;    // Strobe for parallel input data
    private ObservableValue bsy;    // Busy processing parallel input data
    private ObservableValue avail;  // Parallel output data available at Q
    private ObservableValue ack;    // Acknowledge that data is read from Q

    private PortMode portMode;       // serial/parallel
    private boolean portTelnet;      // false=raw, true=telnet
    private boolean lastClock = false;
    private int bsyTicks = 0;
    private long pinVal = 0;
    private long poutVal = 0;
    private long availVal = 0;
    private long bsyVal = 0;
    private long soutVal=1;     // The serial output should idle high

    private boolean lastStb = false;
    private boolean lastAck = false;

    private static final int  OVERSAMPLECNT = 16;
    private static final int  BITS_TO_COLLECT = 1+8+1; // 1 start, 8 data, 1 stop
    private int rxTicks = 0;    // Keeps track of the incoming baud/bitrate clock
    private int txTicks = 0;    // Keeps track of the incoming baud/bitrate clock
    private int txData = 0;     // Bit pattern (including start/stop bits to be sent)
    private int rxData=0;       // Collects bits from sin-input into a byte
    private int rxBitCnt=0;     // How many bits remanining to be collected
    private boolean waitForStartbit=true;

    static private Deque<Integer> buf = new ArrayDeque<Integer>(1000);
    static private PortSocket portSocket = null;


    /**
     * Creates a new instance
     *
     * @param attributes the elements attributes
     */
    public Port(ElementAttributes attributes) {
        portMode = attributes.get(Keys.PORT_MODE);
        portTelnet = attributes.get(Keys.PORT_TELNET);
        label = attributes.getLabel();

        if (portMode==PortMode.serial) { // Serial mode
            sout = new ObservableValue("out", 1).setPinDescription(DESCRIPTION);
        } else {        // Parallel mode
            pout = new ObservableValue("Q", 8).setPinDescription(DESCRIPTION);
            bsy = new ObservableValue("bsy", 1).setPinDescription(DESCRIPTION);
            avail = new ObservableValue("avail", 1).setPinDescription(DESCRIPTION);
        }

        // Create a socket listener thread if we don't already have one
        if (portSocket == null) {
            portSocket = new PortSocket(this, 2323, portTelnet);
            portSocket.start();
        }
    }

    /**
     *
     */
    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        if (portMode==PortMode.serial) {
            // Serial
            sin = inputs.get(0).addObserverToValue(this).checkBits(1, this, 0);
            clock = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
        } else {
            // Parallel
            pin = inputs.get(0).addObserverToValue(this).checkBits(8, this, 0);
            clock = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
            stb = inputs.get(2).addObserverToValue(this).checkBits(1, this, 2);
            ack = inputs.get(3).addObserverToValue(this).checkBits(1, this, 3);
        }
    }

    @Override
    public ObservableValues getOutputs() {
        if (portMode==PortMode.serial) {
            return new ObservableValues(sout);              // Serial
        } else {
            return new ObservableValues(pout, bsy, avail);  // Parallel
        }
    }

    @Override
    public void readInputs() throws NodeException {
        boolean nowClock = clock.getBool();
        boolean rising=(lastClock != nowClock && nowClock);

        if (portMode==PortMode.serial) {
            serialHandlerSout(rising);    // Toggles the sout pin
            serialHandlerSin(rising);     // Collects bits from the sin pin
        } else {
                parallelHandlerD(rising);     // Checks stb and reads D-pin
                parallelHandlerQ(rising);     // Checks buf-queue and writes to Q-pin
        }

        lastClock=nowClock;
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (portMode==PortMode.serial) {
            sout.setValue(soutVal);     // Serial
        } else {
            pout.set(poutVal, 0);       // Parallel
            avail.setValue(availVal);
            bsy.setValue(bsyVal);
        }
    }

    @Override
    public void init(Model model) throws NodeException {
        buf.clear();    // Clear the buffer when the simulation is started
    }

    /**
     *
     * Receives bytes from the socket and send as a serial bit stream
     * at the sout pin
     *
     * If there's pending data in the buffer and the last transmission
     * is done then get data and begin.
     *
     * Original bits   ---- ---- ---- ---- ---- ---- 7654 3210
     * Translated bits ---- ---- ---- ---- ---- -SS7 6543 210s
     * s=startbit (low)  S=stopbit (high)
     *
     * The data is sent as 8N2
     */
    private void serialHandlerSout(boolean rising) {
        if (!rising) return;
        if (!buf.isEmpty() && txData==0) {
            // Get data and tack on the start and stop bits to it
            txData=(buf.remove()<<1) | 0x00000600;
            txTicks=OVERSAMPLECNT;
        }

        // If the data is not fully sent
        // then peel off a bit and output it to the sout pin
        if (txData!=0) {
            if (--txTicks <= 0) {
                txTicks=OVERSAMPLECNT;
                soutVal=txData&1;
                txData>>=1;
            }
        }
    }


    /*
     * Collects serial stream bits from sin pin and sends bytes to the socket
     */
    private void serialHandlerSin(boolean rising) {
        boolean nowSin=sin.getBool();

        if (!rising) return;

        // If waiting for the startbit wait until line goes low, then initialize
        // all involved variables
        if (waitForStartbit) {
            if (!nowSin) {
                waitForStartbit = false;
                rxData = 0;
                rxBitCnt = BITS_TO_COLLECT;
                // Test startbit again in the middle of the bit period
                rxTicks=OVERSAMPLECNT/2;
            }
            return;
        }

        if (--rxTicks == 0) {
            // Are we at the middle of the start bit? If so then
            // re-sample it to check if the previous edge was just
            // a glitch
            if (rxBitCnt==BITS_TO_COLLECT && nowSin) {
                waitForStartbit = true;
                return;
            }
            rxTicks=OVERSAMPLECNT;

            // Shift in the data
            rxData >>= 1;
            if (nowSin) rxData+=(int) Math.pow(2, BITS_TO_COLLECT-1);

            if (--rxBitCnt == 0) {
                waitForStartbit = true;
                // Mask off the start/stopbits and send to socket
                toSocket((rxData>>1)&0xFF);
            }
        }
    }

    /*
     * If rising edge on stb then send the parallel-in (pin) to
     * the socket, and set the bsy flag (it will be automatically
     * be cleared in X clock cycles to simulate a slow external device)
     */
    private void parallelHandlerD(boolean rising) {
        boolean nowStb = stb.getBool();

        if (lastStb != nowStb) {
            if (nowStb) {
                if (!bsy.getBool()) {
                    toSocket((int) pin.getValue());
                    bsyVal=1;
                    bsyTicks=100;
                }
            }
            lastStb=nowStb;
        }

        // Check if it's time to clear the bsy flag, the countdown
        // is only done at risging edges of the clock
        if (rising && bsyTicks>0) {
            if (--bsyTicks==0) bsyVal=0;
        }
    }

    /*
     * If there's pending data in the buffer and the last data
     * has been ack'ed then update the Q port and set the avail flag.
     *
     * Also check for rising edge on ack and then clear the avail flag
     */
    private void parallelHandlerQ(boolean rising) {
        boolean nowAck = ack.getBool();

        if (!buf.isEmpty() && !avail.getBool() && rising) {
            poutVal=buf.remove();
            availVal=1;
        }

        if (lastAck != nowAck) {
            if (nowAck) availVal=0;
            lastAck=nowAck;
        }
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
