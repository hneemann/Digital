package de.neemann.digital.core.memory;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

import java.io.File;

/**
 * @author hneemann
 */
public class ROM extends Node implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(ROM.class, "A", "sel")
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.AddrBits)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.ShowList)
            .addAttribute(AttributeKey.Data);

    private final DataField data;
    private final ObservableValue output;
    private final int addrBits;
    private final boolean showList;
    private final File listFile;
    private ObservableValue addrIn;
    private ObservableValue selIn;
    private int addr;
    private boolean sel;
    private int romAddr;

    public ROM(ElementAttributes attr) {
        int bits = attr.get(AttributeKey.Bits);
        output = new ObservableValue("D", bits, true);
        data = attr.get(AttributeKey.Data);
        addrBits = attr.get(AttributeKey.AddrBits);
        showList = attr.get(AttributeKey.ShowList);
        if (showList)
            listFile = attr.getFile("lastDataFile");
        else
            listFile = null;
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        addrIn = inputs[0].checkBits(addrBits, this).addObserver(this);
        selIn = inputs[1].checkBits(1, this).addObserver(this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    @Override
    public void readInputs() throws NodeException {
        addr = (int) addrIn.getValue();
        sel = selIn.getBool();
        if (sel)
            romAddr = addr;
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.set(data.getData(addr), !sel);
    }

    public ObservableValue getAddrIn() {
        return addrIn;
    }

    public long getRomAddress() {
        return romAddr;
    }

    public File getListFile() {
        return listFile;
    }

    @Override
    public void setModel(Model model) {
        super.setModel(model);
        if (showList && listFile != null)
            model.addRomListing(this);
    }
}
