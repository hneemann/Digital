/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.graphics;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.RAMInterface;

import javax.swing.*;

import java.util.concurrent.atomic.AtomicBoolean;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * Graphic card.
 * Essentially a RAM module with an additional input bit which selects the visible bank.
 * So you can use double buffering.
 */
public class GraphicCard extends Node implements Element, RAMInterface {

    /**
     * The terminal description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(GraphicCard.class,
            input("A"),
            input("str"),
            input("C").setClock(),
            input("ld"),
            input("B"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.GRAPHIC_WIDTH)
            .addAttribute(Keys.GRAPHIC_HEIGHT);

    private final DataField memory;
    private final int width;
    private final int height;
    private final int bankSize;

    private GraphicDialog graphicDialog;
    private final int size;
    private final String label;
    private final int bits;
    private final int addrBits;
    private ObservableValue dataOut;
    private ObservableValue addrIn;
    private ObservableValue strIn;
    private ObservableValue clkIn;
    private ObservableValue ldIn;
    private ObservableValue dataIn;
    private ObservableValue bankIn;
    private boolean lastClk;
    private boolean ld;
    private int addr;
    private boolean lastBank;
    private boolean runningInMainFrame;

    /**
     * Creates a new Graphics instance
     *
     * @param attr the attributes
     */
    public GraphicCard(ElementAttributes attr) {
        label = attr.getLabel();
        width = attr.get(Keys.GRAPHIC_WIDTH);
        height = attr.get(Keys.GRAPHIC_HEIGHT);
        bankSize = width * height;
        bits = attr.get(Keys.BITS);
        size = bankSize * 2;

        int aBits = 1;
        while (((1 << aBits) < size)) aBits++;

        addrBits = aBits;
        memory = new DataField(size);

        dataOut = new ObservableValue("D", bits)
                .setToHighZ()
                .setPinDescription(DESCRIPTION)
                .setBidirectional();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        addrIn = inputs.get(0).checkBits(addrBits, this).addObserverToValue(this);
        strIn = inputs.get(1).checkBits(1, this).addObserverToValue(this);
        clkIn = inputs.get(2).checkBits(1, this).addObserverToValue(this);
        ldIn = inputs.get(3).checkBits(1, this).addObserverToValue(this);
        bankIn = inputs.get(4).checkBits(1, this).addObserverToValue(this);
        dataIn = inputs.get(5).checkBits(bits, this).addObserverToValue(this); // additional input to read the port
    }

    @Override
    public ObservableValues getOutputs() {
        return dataOut.asList();
    }

    @Override
    public void readInputs() throws NodeException {
        long data = 0;
        boolean clk = clkIn.getBool();
        boolean str;
        if (!lastClk && clk) {
            str = strIn.getBool();
            if (str)
                data = dataIn.getValue();
        } else
            str = false;

        ld = ldIn.getBool();
        if (ld || str)
            addr = (int) addrIn.getValue();

        boolean bank = bankIn.getBool();

        if (str) {
            memory.setData(addr, data);
            if (addr >= bankSize == bank)
                updateGraphic(bank);
        }

        if (lastBank != bank)
            updateGraphic(bank);

        lastBank = bank;

        lastClk = clk;
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (ld) {
            dataOut.setValue(memory.getDataWord(addr));
        } else {
            dataOut.setToHighZ();
        }
    }

    @Override
    public void init(Model model) throws NodeException {
        model.addObserver(event -> runningInMainFrame = model.runningInMainFrame(), ModelEventType.STARTED);
    }

    @Override
    public DataField getMemory() {
        return memory;
    }

    private final AtomicBoolean paintPending = new AtomicBoolean();

    private void updateGraphic(boolean bank) {
        if (runningInMainFrame) {
            if (paintPending.compareAndSet(false, true)) {
                SwingUtilities.invokeLater(() -> {
                    if (graphicDialog == null || !graphicDialog.isVisible()) {
                        graphicDialog = new GraphicDialog(getModel().getWindowPosManager().getMainFrame(), width, height);
                        getModel().getWindowPosManager().register("GraphicCard_" + label, graphicDialog);
                    }
                    paintPending.set(false);
                    graphicDialog.updateGraphic(memory, bank);
                });
            }
        }
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getDataBits() {
        return bits;
    }

    @Override
    public int getAddrBits() {
        return addrBits;
    }

    @Override
    public boolean isProgramMemory() {
        return false;
    }

    @Override
    public void setProgramMemory(DataField dataField) {
        memory.setDataFrom(dataField);
    }
}
