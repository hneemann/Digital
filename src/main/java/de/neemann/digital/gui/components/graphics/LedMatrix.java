/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.graphics;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;

import javax.swing.*;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * LED-Matrix
 */
public class LedMatrix extends Node implements Element {

    /**
     * the LED-Matrix description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(LedMatrix.class, input("r-data"), input("c-addr"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.ROW_DATA_BITS)
            .addAttribute(Keys.COL_ADDR_BITS)
            .addAttribute(Keys.COLOR)
            .addAttribute(Keys.LED_PERSISTENCE);

    private final int rowDataBits;
    private final int colAddrBits;
    private final int dx;
    private final int dy;
    private final long[] data;
    private final Color color;
    private final boolean ledPersist;
    private final String label;
    private ObservableValue rowDataVal;
    private ObservableValue colAddrVal;
    private LedMatrixDialog ledMatrixDialog;

    /**
     * create a new instance
     *
     * @param attr the attributes of the element
     */
    public LedMatrix(ElementAttributes attr) {
        rowDataBits = attr.get(Keys.ROW_DATA_BITS);
        colAddrBits = attr.get(Keys.COL_ADDR_BITS);
        label = attr.getLabel();
        color = attr.get(Keys.COLOR);
        ledPersist = attr.get(Keys.LED_PERSISTENCE);
        dx = 1 << colAddrBits;
        dy = rowDataBits;
        data = new long[dx];
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        rowDataVal = inputs.get(0).checkBits(rowDataBits, this, 0).addObserverToValue(this);
        colAddrVal = inputs.get(1).checkBits(colAddrBits, this, 1).addObserverToValue(this);
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void readInputs() throws NodeException {
        long rowData = rowDataVal.getValue();
        int colAddr = (int) colAddrVal.getValue();
        if (colAddr < dx && (!ledPersist || data[colAddr] != rowData)) {
            data[colAddr] = rowData;
            dataChanged(colAddr, rowData);
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

    private final AtomicBoolean paintPending = new AtomicBoolean();

    private void dataChanged(int colAddr, long rowData) {
        if (paintPending.compareAndSet(false, true)) {
            SwingUtilities.invokeLater(() -> {
                if (ledMatrixDialog == null || !ledMatrixDialog.isVisible()) {
                    ledMatrixDialog = new LedMatrixDialog(getModel().getWindowPosManager().getMainFrame(), dy, data, color, ledPersist);
                    getModel().getWindowPosManager().register("ledMatrix_" + label, ledMatrixDialog);
                }
                paintPending.set(false);
                ledMatrixDialog.updateGraphic(colAddr, rowData);
            });
        }
    }
}
