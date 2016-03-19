package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.part.Part;
import de.neemann.digital.core.part.PartAttributes;
import de.neemann.digital.core.part.PartTypeDescription;

/**
 * @author hneemann
 */
public class T_FF extends Node implements Part {

    public static final PartTypeDescription DESCRIPTION = new PartTypeDescription(T_FF.class, "C");
    private ObservableValue clockVal;
    private ObservableValue q;
    private ObservableValue qn;
    private boolean lastClock;
    private boolean out;

    public T_FF(PartAttributes attributes) {
        this.q = new ObservableValue("Q", 1);
        this.qn = new ObservableValue("~Q", 1);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockVal.getBool();
        if (clock && !lastClock) {
            out = !out;
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
        clockVal = inputs[0];
        clockVal.addObserver(this);

        if (clockVal.getBits() != 1)
            throw new BitsException("carryIsABit", clockVal);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{q, qn};
    }

}
