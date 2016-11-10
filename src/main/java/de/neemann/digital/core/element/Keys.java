package de.neemann.digital.core.element;

import de.neemann.digital.core.io.IntFormat;
import de.neemann.digital.core.memory.DataField;
import de.neemann.gui.language.Language;

import java.awt.*;
import java.util.Locale;

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
            = new Key.KeyBits("Bits");

    /**
     * number of inputs in simple gates like And and Or
     */
    public static final Key.KeyInteger INPUT_COUNT
            = new Key.KeyInteger("Inputs", 2)
            .setComboBoxValues(new Integer[]{2, 3, 4, 5})
            .setMin(2);
    /**
     * The elements label
     */
    public static final Key<String> LABEL
            = new Key<>("Label", "");
    /**
     * The size of a LED
     */
    public static final Key.KeyInteger SIZE
            = new Key.KeyInteger("Size", 1)
            .setComboBoxValues(new Integer[]{1, 2, 3, 4, 5})
            .setMin(1);
    /**
     * The value of constants
     */
    public static final Key<Integer> VALUE
            = new Key<>("Value", 1);

    /**
     * The default value of inputs
     */
    public static final Key<Integer> DEFAULT
            = new Key<>("Default", 0);

    /**
     * Color of LEDs
     */
    public static final Key<java.awt.Color> COLOR
            = new Key<>("Color", java.awt.Color.RED);

    /**
     * Background Color of nested circuits
     */
    public static final Key<java.awt.Color> BACKGROUND_COLOR
            = new Key<>("Color", new Color(255, 255, 0, 64));

    /**
     * The input splitting of a splitter
     */
    public static final Key<String> INPUT_SPLIT
            = new Key<>("Input Splitting", "8,8");

    /**
     * The output splitting of a splitter
     */
    public static final Key<String> OUTPUT_SPLIT
            = new Key<>("Output Splitting", "16");

    /**
     * The real time frequency of the clock
     */
    public static final Key<Integer> FREQUENCY
            = new Key.KeyInteger("Frequency", 1)
            .setComboBoxValues(new Integer[]{1, 2, 5, 10, 20, 50, 100, 200, 500, 5000, 50000, 500000})
            .setMin(1);
    /**
     * the bit count of a muxer or decoder
     */
    public static final Key.KeyBits SELECTOR_BITS
            = new Key.KeyBits("Selector Bits");
    /**
     * number of address bits of memory
     */
    public static final Key.KeyBits ADDR_BITS
            = new Key.KeyBits("Addr Bits");

    /**
     * signed flag for comparator element
     */
    public static final Key<Boolean> BLOWN
            = new Key<>("Blown", false);

    /**
     * signed flag for comparator element
     */
    public static final Key<Boolean> SIGNED
            = new Key<>("Signed", false);

    /**
     * the data key for memory
     */
    public static final Key<DataField> DATA
            = new Key<>("Data", DataField.DEFAULT);
    /**
     * flag for flipping selector pos in muxers, decoders and drivers
     */
    public static final Key<Boolean> FLIP_SEL_POSITON
            = new Key<>("flipSelPos", false);
    /**
     * the rotation of the elements
     */
    public static final Key<Rotation> ROTATE
            = new Key<>("rotation", new Rotation(0));

    /**
     * the width of an element if it is included as nested element
     */
    public static final Key.KeyInteger WIDTH
            = new Key.KeyInteger("Width", 3)
            .setMin(2);

    /**
     * width of the terminal
     */
    public static final Key.KeyInteger TERM_WIDTH
            = new Key.KeyInteger("termWidth", 50)
            .setMin(10);

    /**
     * height of the terminal
     */
    public static final Key.KeyInteger TERM_HEIGHT
            = new Key.KeyInteger("termHeight", 25)
            .setMin(5);

    /**
     * break timeout cycles
     */
    public static final Key.KeyInteger CYCLES
            = new Key.KeyInteger("Cycles", 100000)
            .setComboBoxValues(new Integer[]{1000, 10000, 100000, 1000000});
    /**
     * flag to make a value a probe
     */
    public static final Key<Boolean> VALUE_IS_PROBE
            = new Key<>("valueIsProbe", false);

    /**
     * flag to enable the ROMs listings view
     */
    public static final Key<Boolean> SHOW_LISTING
            = new Key<>("showList", false);

    /**
     * flag to set a ROM as program memory
     */
    public static final Key<Boolean> IS_PROGRAM_MEMORY
            = new Key<>("isProgramMemory", false);

    /**
     * flag to enable the ROMs auto load function
     */
    public static final Key<Boolean> AUTO_RELOAD_ROM
            = new Key<>("autoReload", false);

    /**
     * flag to show the data table window
     */
    public static final Key<Boolean> SHOW_DATA_TABLE
            = new Key<>("showDataTable", false);
    /**
     * flag to show the data graph window
     */
    public static final Key<Boolean> SHOW_DATA_GRAPH
            = new Key<>("showDataGraph", false);

    /**
     * flag to show the data graph window in single gate mode
     */
    public static final Key<Boolean> SHOW_DATA_GRAPH_MICRO
            = new Key<>("showDataGraphMicro", false);

    /**
     * flag to enable the single gate mode in the embedded data view
     */
    public static final Key<Boolean> MICRO_STEP
            = new Key<>("microStep", false);

    /**
     * the max number of samples in the embedded data view
     */
    public static final Key.KeyInteger MAX_STEP_COUNT
            = new Key.KeyInteger("maxStepCount", 25)
            .setMin(5);
    /**
     * flag to enable high z mode at an input
     */
    public static final Key<Boolean> IS_HIGH_Z
            = new Key<>("isHighZ", false);

    /**
     * flag to enable realtime mode at a clock
     */
    public static final Key<Boolean> RUN_AT_REAL_TIME
            = new Key<>("runRealTime", false);

    /**
     * the description of an element
     */
    public static final Key.LongString DESCRIPTION
            = new Key.LongString("Description");

    /**
     * A net name
     */
    public static final Key<String> NETNAME
            = new Key<>("NetName", "");

    /**
     * shape setting
     */
    public static final Key<Boolean> SETTINGS_IEEE_SHAPES
            = new Key<>("IEEEShapes", Locale.getDefault().getLanguage().equals(Locale.US.getLanguage()));

    /**
     * The Gui Language
     */
    public static final Key<Language> SETTINGS_LANGUAGE
            = new Key<>("Language", new Language());


    /**
     * output format for numbers
     */
    public static final Key.KeyEnum<IntFormat> INTFORMAT
            = new Key.KeyEnum<>("intFormat", IntFormat.def, IntFormat.values());


    /**
     * width of the terminal
     */
    public static final Key.KeyInteger GRAPHIC_WIDTH
            = new Key.KeyInteger("graphicWidth", 160)
            .setMin(5);

    /**
     * height of the terminal
     */
    public static final Key.KeyInteger GRAPHIC_HEIGHT
            = new Key.KeyInteger("graphicHeight", 100)
            .setMin(5);

}
