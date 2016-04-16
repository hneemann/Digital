package de.neemann.digital.core.element;

import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.lang.Lang;

/**
 * Collection of key constants
 *
 * @author hneemann
 */
public final class Keys {

    private Keys() {
    }

    /**
     * number of bits in simple gates like And and Or
     */
    public static final Key.KeyBits BITS
            = new Key.KeyBits("Bits", Lang.get("key_dataBits"));

    /**
     * number of inputs in simple gates like And and Or
     */
    public static final Key.KeyInteger INPUT_COUNT
            = new Key.KeyInteger("Inputs", Lang.get("key_numberInputs"), 2)
            .setComboBoxValues(new Integer[]{2, 3, 4, 5})
            .setMin(2);
    /**
     * The elements label
     */
    public static final Key<String> LABEL
            = new Key<>("Label", Lang.get("key_label"), "");

    /**
     * The value of constants
     */
    public static final Key<Integer> VALUE
            = new Key<>("Value", Lang.get("key_value"), 1);

    /**
     * The default value of inputs
     */
    public static final Key<Integer> DEFAULT
            = new Key<>("Default", Lang.get("key_default"), 0);

    /**
     * Color of LEDs
     */
    public static final Key<java.awt.Color> COLOR
            = new Key<>("Color", Lang.get("key_color"), java.awt.Color.RED);
    /**
     * The input splitting of a splitter
     */
    public static final Key<String> INPUT_SPLIT
            = new Key<>("Input Splitting", Lang.get("key_inputSplitting"), "");

    /**
     * The output splitting of a splitter
     */
    public static final Key<String> OUTPUT_SPLIT
            = new Key<>("Output Splitting", Lang.get("key_outputSplitting"), "");

    /**
     * The real time frequency of the clock
     */
    public static final Key<Integer> FREQUENCY
            = new Key.KeyInteger("Frequency", Lang.get("key_frequency"), 1)
            .setComboBoxValues(new Integer[]{1, 2, 5, 10, 20, 50, 100, 200, 500})
            .setMin(1);
    /**
     * the bit count of a muxer or decoder
     */
    public static final Key.KeyBits SELECTOR_BITS
            = new Key.KeyBits("Selector Bits", Lang.get("key_selectorBits"));
    /**
     * number of address bits of memory
     */
    public static final Key.KeyBits ADDR_BITS
            = new Key.KeyBits("Addr Bits", Lang.get("key_addrBits"));

    /**
     * signed flag for comparator element
     */
    public static final Key<Boolean> SIGNED
            = new Key<>("Signed", Lang.get("key_signed"), false);

    /**
     * the data key for memory
     */
    public static final Key<DataField> DATA
            = new Key<>("Data", Lang.get("key_data"), DataField.DEFAULT);
    /**
     * flag for flipping selector pos in muxers, decoders and drivers
     */
    public static final Key<Boolean> FLIP_SEL_POSITON
            = new Key<>("flipSelPos", Lang.get("key_flipSelPos"), false);
    /**
     * the rotation of the elements
     */
    public static final Key<Rotation> ROTATE
            = new Key<>("rotation", Lang.get("key_rotation"), new Rotation(0));

    /**
     * the width of an element if it is included as nested element
     */
    public static final Key.KeyInteger WIDTH
            = new Key.KeyInteger("Width", Lang.get("key_width"), 3)
            .setMin(2);

    /**
     * width of the terminal
     */
    public static final Key.KeyInteger TERM_WIDTH
            = new Key.KeyInteger("termWidth", Lang.get("key_termWidth"), 50)
            .setMin(10);

    /**
     * height of the terminal
     */
    public static final Key.KeyInteger TERM_HEIGHT
            = new Key.KeyInteger("termHeight", Lang.get("key_termHeight"), 25)
            .setMin(5);

    /**
     * break timeout cycles
     */
    public static final Key.KeyInteger CYCLES
            = new Key.KeyInteger("Cycles", Lang.get("key_cycles"), 100000)
            .setComboBoxValues(new Integer[]{1000, 10000, 100000, 1000000});
    /**
     * flag to make a value a probe
     */
    public static final Key<Boolean> VALUE_IS_PROBE
            = new Key<>("valueIsProbe", Lang.get("key_valueIsProbe"), false);

    /**
     * flag to enable the ROMs listings view
     */
    public static final Key<Boolean> SHOW_LISTING
            = new Key<>("showList", Lang.get("key_showListing"), false);

    /**
     * flag to show the data table window
     */
    public static final Key<Boolean> SHOW_DATA_TABLE
            = new Key<>("showDataTable", Lang.get("key_showDataTable"), false);
    /**
     * flag to show the data graph window
     */
    public static final Key<Boolean> SHOW_DATA_GRAPH
            = new Key<>("showDataGraph", Lang.get("key_showDataGraph"), false);

    /**
     * flag to show the data graph window in single gate mode
     */
    public static final Key<Boolean> SHOW_DATA_GRAPH_MICRO
            = new Key<>("showDataGraphMicro", Lang.get("key_showDataGraphMicro"), false);

    /**
     * flag to enable the single gate mode in the embedded data view
     */
    public static final Key<Boolean> MICRO_STEP
            = new Key<>("microStep", Lang.get("key_microStep"), false);

    /**
     * the max number of samples in the embedded data view
     */
    public static final Key.KeyInteger MAX_STEP_COUNT
            = new Key.KeyInteger("maxStepCount", Lang.get("key_maxStepCount"), 25)
            .setMin(5);
    /**
     * flag to enable high z mode at an input
     */
    public static final Key<Boolean> IS_HIGH_Z
            = new Key<>("isHighZ", Lang.get("key_isHighZ"), false);

    /**
     * flag to enable realtime mode at a clock
     */
    public static final Key<Boolean> RUN_AT_REAL_TIME
            = new Key<>("runRealTime", Lang.get("key_runRealTime"), false);

    /**
     * the description of an element
     */
    public static final Key<String> DESCRIPTION
            = new Key<>("Description", Lang.get("key_description"), "");

}
