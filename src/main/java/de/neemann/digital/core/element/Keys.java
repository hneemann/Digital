package de.neemann.digital.core.element;

import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.lang.Lang;

/**
 * A key for a single attribute
 *
 * @param <VALUE> the type of the value
 * @author hneemann
 */
public final class Keys<VALUE> {

    private Keys() {
    }

    public static final Key.KeyInteger Bits
            = new Key.KeyInteger("Bits", Lang.get("key_dataBits"), 1)
            .setComboBoxValues(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
    public static final Key.KeyInteger InputCount
            = new Key.KeyInteger("Inputs", Lang.get("key_numberInputs"), 2)
            .setComboBoxValues(new Integer[]{1, 2, 3, 4, 5});
    public static final Key<String> Label
            = new Key<>("Label", Lang.get("key_label"), "");
    public static final Key<Integer> Value
            = new Key<>("Value", Lang.get("key_value"), 1);
    public static final Key<Integer> Default
            = new Key<>("Default", Lang.get("key_default"), 0);
    public static final Key<java.awt.Color> Color
            = new Key<>("Color", Lang.get("key_color"), java.awt.Color.RED);
    public static final Key<String> InputSplit
            = new Key<>("Input Splitting", Lang.get("key_inputSplitting"), "");
    public static final Key<String> OutputSplit
            = new Key<>("Output Splitting", Lang.get("key_outputSplitting"), "");
    public static final Key<Integer> Frequency
            = new Key.KeyInteger("Frequency", Lang.get("key_frequency"), 1)
            .setComboBoxValues(new Integer[]{1, 2, 5, 10, 20, 50, 100, 200, 500});
    public static final Key<Integer> SelectorBits
            = new Key<>("Selector Bits", Lang.get("key_selectorBits"), 1);
    public static final Key<Integer> AddrBits
            = new Key<>("Addr Bits", Lang.get("key_addrBits"), InputCount.getDefault());
    public static final Key<Boolean> Signed
            = new Key<>("Signed", Lang.get("key_signed"), false);
    public static final Key<DataField> Data
            = new Key<>("Data", Lang.get("key_data"), DataField.DEFAULT);
    public static final Key<Boolean> FlipSelPositon
            = new Key<>("flipSelPos", Lang.get("key_flipSelPos"), false);
    public static final Key<Rotation> Rotate
            = new Key<>("rotation", Lang.get("key_rotation"), new Rotation(0));
    public static final Key<Integer> Width
            = new Key<>("Width", Lang.get("key_width"), 3);
    public static final Key<Integer> TermWidth
            = new Key<>("termWidth", Lang.get("key_termWidth"), 50);
    public static final Key<Integer> TermHeight
            = new Key<>("termHeight", Lang.get("key_termHeight"), 25);
    public static final Key.KeyInteger Cycles
            = new Key.KeyInteger("Cycles", Lang.get("key_cycles"), 100000)
            .setComboBoxValues(new Integer[]{1000, 10000, 100000, 1000000});
    public static final Key<Boolean> ValueIsProbe
            = new Key<>("valueIsProbe", Lang.get("key_valueIsProbe"), false);
    public static final Key<Boolean> ShowListing
            = new Key<>("showList", Lang.get("key_showListing"), false);

    public static final Key<Boolean> ShowDataTable
            = new Key<>("showDataTable", Lang.get("key_showDataTable"), false);
    public static final Key<Boolean> ShowDataGraph
            = new Key<>("showDataGraph", Lang.get("key_showDataGraph"), false);
    public static final Key<Boolean> ShowDataGraphMicro
            = new Key<>("showDataGraphMicro", Lang.get("key_showDataGraphMicro"), false);
    public static final Key<Boolean> MicroStep
            = new Key<>("microStep", Lang.get("key_microStep"), false);
    public static final Key<Integer> MaxStepCount
            = new Key<>("maxStepCount", Lang.get("key_maxStepCount"), 25);

    public static final Key<Boolean> IsHighZ
            = new Key<>("isHighZ", Lang.get("key_isHighZ"), false);
    public static final Key<Boolean> RunAtRealTime
            = new Key<>("runRealTime", Lang.get("key_runRealTime"), false);
    public static final Key<String> Description
            = new Key<>("Description", Lang.get("key_description"), "");

}
