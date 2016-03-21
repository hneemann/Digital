package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public class RS_FF extends Node implements Element {

    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(RS_FF.class, "R", "C", "S")
            .setShortName("RS");

    private ObservableValue jVal;
    private ObservableValue kVal;
    private ObservableValue clockVal;
    private ObservableValue q;
    private ObservableValue qn;
    private boolean lastClock;
    private boolean out;

    public RS_FF(ElementAttributes attributes) {
        this.q = new ObservableValue("Q", 1);
        this.qn = new ObservableValue("\u00ACQ", 1);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockVal.getBool();
        if (clock && !lastClock) {
            boolean j = jVal.getBool();
            boolean k = kVal.getBool();

            if (j && !k) out = true;
            else if (!j && k) out = false;
        }
        lastClock = clock;
    }

    @Override
    public void writeOutputs() throws NodeException {
        q.setBool(out);
        qn.setBool(!out);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws BitsException {
        jVal = inputs[0].addObserver(this);
        clockVal = inputs[1].addObserver(this);
        kVal = inputs[2].addObserver(this);

        if (jVal.getBits() != 1)
            throw new BitsException("wrongBitCount", jVal);

        if (kVal.getBits() != 1)
            throw new BitsException("wrongBitCount", kVal);

        if (clockVal.getBits() != 1)
            throw new BitsException("carryIsABit", clockVal);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{q, qn};
    }

}
