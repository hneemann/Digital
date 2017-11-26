package de.neemann.digital.core.memory;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import java.io.File;
import java.io.IOException;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A ROM module.
 *
 * @author hneemann
 */
public class ROM extends Node implements Element {
    /**
     * Key used to store the source file in the attribute set
     */
    public final static String LAST_DATA_FILE_KEY = "lastDataFile";

    /**
     * The ROMs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(ROM.class,
            input("A"),
            input("sel"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DATA)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(Keys.AUTO_RELOAD_ROM);

    private DataField data;
    private final ObservableValue output;
    private final int addrBits;
    private final File hexFile;
    private final boolean autoLoad;
    private final boolean isProgramMemory;
    private ObservableValue addrIn;
    private ObservableValue selIn;
    private int addr;
    private boolean sel;
    private int romAddr;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public ROM(ElementAttributes attr) {
        int bits = attr.get(Keys.BITS);
        output = new ObservableValue("D", bits, true).setPinDescription(DESCRIPTION);
        data = attr.get(Keys.DATA);
        addrBits = attr.get(Keys.ADDR_BITS);
        autoLoad = attr.get(Keys.AUTO_RELOAD_ROM);
        isProgramMemory = attr.get(Keys.IS_PROGRAM_MEMORY);
        if (autoLoad) {
            hexFile = attr.getFile(LAST_DATA_FILE_KEY);
        } else
            hexFile = null;
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        addrIn = inputs.get(0).checkBits(addrBits, this, 0).addObserverToValue(this);
        selIn = inputs.get(1).checkBits(1, this, 1).addObserverToValue(this);
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void readInputs() throws NodeException {
        addr = (int) addrIn.getValue();
        sel = selIn.getBool();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.set(data.getDataWord(addr), !sel);
    }

    /**
     * @return the last used input address
     */
    public long getRomAddress() {
        return romAddr;
    }

    @Override
    public void init(Model model) throws NodeException {
        if (autoLoad) {
            try {
                data = new DataField(hexFile);
            } catch (IOException e) {
                throw new NodeException(e.getMessage(), this, -1, null);
            }
        }
    }

    /**
     * Called if the the last ROM address is needed by the remote interface
     *
     * @param model the model
     */
    public void provideRomAdress(Model model) {
        if (isProgramMemory)
            model.addObserver(event -> {
                if (event == ModelEvent.STEP && sel)
                    romAddr = addr;
            }, ModelEvent.STEP);
    }


    /**
     * @return true if this is program memory
     */
    public boolean isProgramMemory() {
        return isProgramMemory;
    }

    /**
     * Sets the data for this ROM element
     *
     * @param data data to use
     */
    public void setData(DataField data) {
        this.data = data;
    }

}
