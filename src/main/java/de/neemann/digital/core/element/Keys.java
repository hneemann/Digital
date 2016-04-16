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

    public static final Key.KeyBits BITS
            = new Key.KeyBits("Bits", Lang.get("key_dataBits"));
    public static final Key.KeyInteger INPUT_COUNT
            = new Key.KeyInteger("Inputs", Lang.get("key_numberInputs"), 2)
            .setComboBoxValues(new Integer[]{2, 3, 4, 5})
            .setMin(2);
    public static final Key<String> LABEL
            = new Key<>("Label", Lang.get("key_label"), "");
    public static final Key<Integer> VALUE
            = new Key<>("Value", Lang.get("key_value"), 1);
    public static final Key<Integer> DEFAULT
            = new Key<>("Default", Lang.get("key_default"), 0);
    public static final Key<java.awt.Color> COLOR
            = new Key<>("Color", Lang.get("key_color"), java.awt.Color.RED);
    public static final Key<String> INPUT_SPLIT
            = new Key<>("Input Splitting", Lang.get("key_inputSplitting"), "");
    public static final Key<String> OUTPUT_SPLIT
            = new Key<>("Output Splitting", Lang.get("key_outputSplitting"), "");
    public static final Key<Integer> FREQUENCY
            = new Key.KeyInteger("Frequency", Lang.get("key_frequency"), 1)
            .setComboBoxValues(new Integer[]{1, 2, 5, 10, 20, 50, 100, 200, 500})
            .setMin(1);
    public static final Key.KeyBits SELECTOR_BITS
            = new Key.KeyBits("Selector Bits", Lang.get("key_selectorBits"));
    public static final Key.KeyBits ADDR_BITS
            = new Key.KeyBits("Addr Bits", Lang.get("key_addrBits"));
    public static final Key<Boolean> SIGNED
            = new Key<>("Signed", Lang.get("key_signed"), false);
    public static final Key<DataField> DATA
            = new Key<>("Data", Lang.get("key_data"), DataField.DEFAULT);
    public static final Key<Boolean> FLIP_SEL_POSITON
            = new Key<>("flipSelPos", Lang.get("key_flipSelPos"), false);
    public static final Key<Rotation> ROTATE
            = new Key<>("rotation", Lang.get("key_rotation"), new Rotation(0));
    public static final Key.KeyInteger WIDTH
            = new Key.KeyInteger("Width", Lang.get("key_width"), 3)
            .setMin(2);
    public static final Key.KeyInteger TERM_WIDTH
            = new Key.KeyInteger("termWidth", Lang.get("key_termWidth"), 50)
            .setMin(10);
    public static final Key.KeyInteger TERM_HEIGHT
            = new Key.KeyInteger("termHeight", Lang.get("key_termHeight"), 25)
            .setMin(5);
    public static final Key.KeyInteger CYCLES
            = new Key.KeyInteger("Cycles", Lang.get("key_cycles"), 100000)
            .setComboBoxValues(new Integer[]{1000, 10000, 100000, 1000000});
    public static final Key<Boolean> VALUE_IS_PROBE
            = new Key<>("valueIsProbe", Lang.get("key_valueIsProbe"), false);
    public static final Key<Boolean> SHOW_LISTING
            = new Key<>("showList", Lang.get("key_showListing"), false);

    public static final Key<Boolean> SHOW_DATA_TABLE
            = new Key<>("showDataTable", Lang.get("key_showDataTable"), false);
    public static final Key<Boolean> SHOW_DATA_GRAPH
            = new Key<>("showDataGraph", Lang.get("key_showDataGraph"), false);
    public static final Key<Boolean> SHOW_DATA_GRAPH_MICRO
            = new Key<>("showDataGraphMicro", Lang.get("key_showDataGraphMicro"), false);
    public static final Key<Boolean> MICRO_STEP
            = new Key<>("microStep", Lang.get("key_microStep"), false);
    public static final Key.KeyInteger MAX_STEP_COUNT
            = new Key.KeyInteger("maxStepCount", Lang.get("key_maxStepCount"), 25)
            .setMin(5);

    public static final Key<Boolean> IS_HIGH_Z
            = new Key<>("isHighZ", Lang.get("key_isHighZ"), false);
    public static final Key<Boolean> RUN_AT_REAL_TIME
            = new Key<>("runRealTime", Lang.get("key_runRealTime"), false);
    public static final Key<String> DESCRIPTION
            = new Key<>("Description", Lang.get("key_description"), "");

}
