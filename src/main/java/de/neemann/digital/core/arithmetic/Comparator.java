package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A comparator
 * @author hneemann
 */
public class Comparator extends Node implements Element {

    /**
     * The comparators description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Comparator.class, input("a"), input("b"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.SIGNED)
            .setShortName("");

    private final int bits;
    private final Boolean signed;
    private final ObservableValue aklb;
    private final ObservableValue equals;
    private final ObservableValue agrb;
    private final int maskAnd;
    private final int maskOr;
    private ObservableValue a;
    private ObservableValue b;
    private long valueA;
    private long valueB;

    /**
     * Create a new instance
     *
     * @param attributes the attributes
     */
    public Comparator(ElementAttributes attributes) {
        signed = attributes.get(Keys.SIGNED);
        bits = attributes.get(Keys.BITS);
        this.maskAnd = 1 << (bits - 1);
        this.maskOr = ~((1 << bits) - 1);

        this.agrb = new ObservableValue(">", 1);
        this.equals = new ObservableValue("=", 1);
        this.aklb = new ObservableValue("<", 1);
    }

    @Override
    public void readInputs() throws NodeException {
        valueA = a.getValue();
        valueB = b.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (valueA == valueB) {
            equals.setValue(1);
            aklb.setValue(0);
            agrb.setValue(0);
        } else {
            equals.setValue(0);

            if (signed) {
                valueA = signeExtend(valueA);
                valueB = signeExtend(valueB);
            }

            boolean kl = valueA < valueB;
            aklb.setBool(kl);
            agrb.setBool(!kl);
        }
    }

    private long signeExtend(long v) {
        if ((v & maskAnd) != 0)
            return v | maskOr;
        else
            return v;
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws BitsException {
        a = inputs[0].addObserverToValue(this).checkBits(bits, this);
        b = inputs[1].addObserverToValue(this).checkBits(bits, this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{agrb, equals, aklb};
    }

}
