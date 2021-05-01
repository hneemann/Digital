/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io.telnet;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import java.io.IOException;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The telnet node
 */
public class Telnet extends Node implements Element {

    /**
     * The telnet server description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Telnet.class,
            input("in"), input("C").setClock(), input("wr"), input("rd"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.TELNET_ESCAPE)
            .addAttribute(Keys.PORT);

    private final ObservableValue dataOut;
    private final ObservableValue dataAvail;
    private final int port;
    private final boolean telnetEscape;
    private ObservableValue dataIn;
    private ObservableValue clockValue;
    private ObservableValue writeEnable;
    private ObservableValue readEnableValue;
    private Server server;
    private boolean lastClock;
    private boolean readEnable;

    /**
     * Creates a new instance
     *
     * @param attributes The components attributes
     */
    public Telnet(ElementAttributes attributes) {
        dataOut = new ObservableValue("out", 8)
                .setToHighZ()
                .setPinDescription(DESCRIPTION);
        dataAvail = new ObservableValue("av", 1)
                .setPinDescription(DESCRIPTION);
        port = attributes.get(Keys.PORT);
        telnetEscape = attributes.get(Keys.TELNET_ESCAPE);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        dataIn = inputs.get(0).checkBits(8, this, 0);
        clockValue = inputs.get(1).checkBits(1, this, 1).addObserverToValue(this);
        writeEnable = inputs.get(2).checkBits(1, this, 2);
        readEnableValue = inputs.get(3).checkBits(1, this, 3).addObserverToValue(this);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockValue.getBool();
        readEnable = readEnableValue.getBool();
        if (clock & !lastClock) {
            if (writeEnable.getBool())
                server.send((int) dataIn.getValue());
            if (readEnable)
                server.deleteOldest();
        }
        lastClock = clock;
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (readEnable)
            dataOut.setValue(server.getData());
        else
            dataOut.setToHighZ();

        dataAvail.setBool(server.hasData());
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return new ObservableValues(dataOut, dataAvail);
    }

    @Override
    public void init(Model model) throws NodeException {
        try {
            server = ServerHolder.INSTANCE.getServer(port);
        } catch (IOException e) {
            throw new NodeException(Lang.get("err_couldNotCreateServer"), e);
        }
        server.setTelnetEscape(telnetEscape);
        server.setTelnetNode(this, model);
    }

}
