package de.neemann.digital.core.element;

import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.core.arithmetic.BarrelShifterMode;
import de.neemann.digital.core.arithmetic.LeftRightFormat;
import de.neemann.digital.core.io.InValue;
import de.neemann.digital.core.IntFormat;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.gui.Screen;
import de.neemann.gui.language.Language;

import java.awt.*;
import java.io.File;
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
            = new Key.KeyBits("Bits", 1);

    /**
     * input bits of sign extender
     */
    public static final Key.KeyBits INPUT_BITS
            = new Key.KeyBits("inputBits", 8);

    /**
     * output bits of sign extender
     */
    public static final Key.KeyBits OUTPUT_BITS
            = new Key.KeyBits("outputBits", 16);

    /**
     * number of inputs in simple gates like And and Or
     */
    public static final Key.KeyInteger INPUT_COUNT  // needs to have the same default value as ADDR_BITS!!!  see de.neemann.digital.gui.components.EditorFactory#DataFieldEditor
            = new Key.KeyInteger("Inputs", 2)
            .setComboBoxValues(new Integer[]{2, 3, 4, 5})
            .setMin(2);


    /**
     * the delay time used by the delay component
     */
    public static final Key.KeyInteger DELAY_TIME
            = new Key.KeyInteger("delayTime", 1)
            .setComboBoxValues(new Integer[]{1, 2, 3, 4, 5})
            .setMin(1)
            .setMax(20);

    /**
     * The elements label
     */
    public static final Key<String> LABEL
            = new Key<>("Label", "");


    /**
     * The font size
     */
    public static final Key<Integer> FONT_SIZE =
            new Key.KeyInteger("textFontSize", Style.NORMAL.getFontSize())
                    .setComboBoxValues(new Integer[]{14, 17, 20, 24, 36, 48, 60})
                    .setMin(10)
                    .setMax(70);

    /**
     * The size of a LED
     */
    public static final Key<Integer> SIZE
            = new Key.KeyInteger("Size", 1)
            .setComboBoxValues(new Integer[]{1, 2, 3, 4, 5})
            .setMin(1)
            .allowGroupEdit();

    /**
     * The value of constants
     */
    public static final Key<Long> VALUE
            = new Key<>("Value", 1L).allowGroupEdit();

    /**
     * The default value of elements
     */
    public static final Key<Integer> DEFAULT
            = new Key<>("Default", 0).allowGroupEdit();

    /**
     * The default value of inputs
     */
    public static final Key<InValue> INPUT_DEFAULT
            = new Key<>("InDefault", new InValue(0)).allowGroupEdit();

    /**
     * Color of LEDs
     */
    public static final Key<java.awt.Color> COLOR
            = new Key<>("Color", java.awt.Color.RED).allowGroupEdit();

    /**
     * The input splitting of a splitter
     */
    public static final Key<String> INPUT_SPLIT
            = new Key<>("Input Splitting", "4,4");

    /**
     * The output splitting of a splitter
     */
    public static final Key<String> OUTPUT_SPLIT
            = new Key<>("Output Splitting", "8");

    /**
     * flag to enable realtime mode at a clock
     */
    public static final Key<Boolean> RUN_AT_REAL_TIME
            = new Key<>("runRealTime", false);

    /**
     * inverts the output of a gate
     */
    public static final Key<Boolean> INVERT_OUTPUT
            = new Key<>("invertOutput", true);

    /**
     * The real time frequency of the clock
     */
    public static final Key<Integer> FREQUENCY
            = new Key.KeyInteger("Frequency", 1)
            .setComboBoxValues(new Integer[]{1, 2, 5, 10, 20, 50, 100, 200, 500, 5000, 50000, 500000})
            .setMin(1)
            .setDependsOn(RUN_AT_REAL_TIME);

    /**
     * the bit count of a muxer or decoder
     */
    public static final Key<Integer> SELECTOR_BITS
            = new Key.KeyBits("Selector Bits", 1).setMax(8);

    /**
     * number of address bits of memory
     */
    public static final Key<Integer> ADDR_BITS
            = new Key.KeyBits("AddrBits", 2).setMax(24); // needs to have the same default value as INPUT_COUNT!!!  see de.neemann.digital.gui.components.EditorFactory#DataFieldEditor

    /**
     * indicates a diode as blown fuse or as programmed
     */
    public static final Key<Boolean> BLOWN
            = new Key<>("Blown", false).allowGroupEdit();

    /**
     * indicates a switch as closed or not
     */
    public static final Key<Boolean> CLOSED
            = new Key<>("Closed", false).allowGroupEdit();

    /**
     * signed flag for comparator element
     */
    public static final Key<Boolean> SIGNED
            = new Key<>("Signed", false).allowGroupEdit();

    /**
     * the data key for memory
     */
    public static final Key<DataField> DATA
            = new Key<>("Data", DataField.DEFAULT);

    /**
     * flag for flipping selector pos in muxers, decoders and drivers
     */
    public static final Key<Boolean> FLIP_SEL_POSITON
            = new Key<>("flipSelPos", false).allowGroupEdit();

    /**
     * the rotation of the elements
     */
    public static final Key<Rotation> ROTATE
            = new Key<>("rotation", new Rotation(0)).allowGroupEdit();

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
            = new Key<>("valueIsProbe", false).allowGroupEdit();

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
            = new Key<>("isHighZ", false).allowGroupEdit();

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
     * The GUI Language
     */
    public static final Key<Language> SETTINGS_LANGUAGE
            = new Key<>("Language", new Language());


    /**
     * Default state of the tree view
     */
    public static final Key<Boolean> SETTINGS_DEFAULT_TREESELECT
            = new Key<>("defTreeSelect", false);

    /**
     * The GUI expression string representation
     */
    public static final Key<FormatToExpression> SETTINGS_EXPRESSION_FORMAT
            = new Key<>("ExpressionFormat", FormatToExpression.FORMATTER_UNICODE);

    /**
     * enables the grid
     */
    public static final Key<Boolean> SETTINGS_GRID
            = new Key<>("grid", false);

    /**
     * enables the wire bits view
     */
    public static final Key<Boolean> SETTINGS_SHOW_WIRE_BITS
            = new Key<>("showWireBits", false);

    /**
     * output format for numbers
     */
    public static final Key.KeyEnum<IntFormat> INT_FORMAT
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

    /**
     * flag used by a relay to indicate if it is normally open or normally closed.
     */
    public static final Key<Boolean> RELAY_NORMALLY_CLOSED
            = new Key<>("relayNormallyClosed", false).allowGroupEdit();

    /**
     * flag used by a barrel shifter to select the shift mode
     */
    public static final Key<BarrelShifterMode> BARREL_SHIFTER_MODE
            = new Key.KeyEnum<>("barrelShifterMode", BarrelShifterMode.logical, BarrelShifterMode.values());

    /**
     * flag used by a barrel shifter to indicate shift direction
     */
    public static final Key<LeftRightFormat> DIRECTION
            = new Key.KeyEnum<>("direction", LeftRightFormat.left, LeftRightFormat.values());

    /**
     * flag used by a barrel shifter to indicate if shift value is signed
     */
    public static final Key<Boolean> BARREL_SIGNED
            = new Key<>("barrelSigned", false);

    /**
     * Used to indicate if the 7-seg display has a common cathode output
     */
    public static final Key<Boolean> COMMON_CATHODE
            = new Key<>("commonCathode", false).allowGroupEdit();

    /**
     * Used to enable the storage of the last state in the Seven Seg display.
     */
    public static final Key<Boolean> LED_PERSISTENCE
            = new Key<>("ledPersistence", false).allowGroupEdit();

    /**
     * Fitter for the atf1502
     */
    public static final Key<File> SETTINGS_ATF1502_FITTER
            = new Key.KeyFile("atf1502Fitter", new File("c:/Wincupl/WinCupl/Fitters")).setDirectoryOnly(true);

    /**
     * row bits in led matrix
     */
    public static final Key.KeyBits ROW_DATA_BITS
            = new Key.KeyBits("rowDataBits", 8);

    /**
     * column address bits in led matrix
     */
    public static final Key.KeyBits COL_ADDR_BITS
            = new Key.KeyBits("colAddrBits", 3);

    /**
     * In locked mode the circuit can not be modified
     */
    public static final Key<Boolean> LOCKED_MODE
            = new Key<>("lockedMode", false);

    /**
     * the pin number
     */
    public static final Key<String> PINNUMBER =
            new Key<>("pinNumber", "");

    /**
     * true if shape is a dil shape
     */
    public static final Key<Boolean> IS_DIL
            = new Key<>("isDIL", false);
    /**
     * the pin count
     */
    public static final Key<Integer> PINCOUNT =
            new Key.KeyInteger("pinCount", 0)
                    .setMin(0)
                    .setDependsOn(IS_DIL);


    /**
     * contains the input inverter config
     */
    public static final Key<InverterConfig> INVERTER_CONFIG
            = new Key<>("inverterConfig", new InverterConfig());

    /**
     * Background Color of nested circuits
     */
    public static final Key<java.awt.Color> BACKGROUND_COLOR
            = new Key<>("backgroundColor", new Color(255, 255, 180, 200))
            .setDependsOn(IS_DIL, true);

    /**
     * the screen resolution
     */
    public static final Key<Integer> SETTINGS_FONT_SCALING =
            new Key.KeyInteger("fontSize", Screen.getDefaultFontScaling())
                    .setComboBoxValues(new Integer[]{100, 120, 150, 180, 200, 250, 300})
                    .setMin(50)
                    .setMax(400);

    /**
     * true if a enable input is needed
     */
    public static final Key<Boolean> WITH_ENABLE
            = new Key<>("withEnable", true);

    /**
     * true to simulate a unidirectional FET
     */
    public static final Key<Boolean> FET_UNIDIRECTIONAL
            = new Key<>("unidirectional", false);

    /**
     * true if component is active low
     */
    public static final Key<Boolean> ACTIVE_LOW
            = new Key<>("activeLow", false).allowGroupEdit();

    /**
     * true if button is mapped to the keyboard
     */
    public static final Key<Boolean> MAP_TO_KEY
            = new Key<>("mapToKey", false).allowGroupEdit();

    /**
     * Fitter for the atf1502
     */
    public static final Key<File> SETTINGS_LIBRARY_PATH
            = new Key.KeyFile("libraryPath", ElementLibrary.getLibPath()).setDirectoryOnly(true);

    /**
     * A jar containing custom java components
     */
    public static final Key<File> SETTINGS_JAR_PATH
            = new Key.KeyFile("jarPath", new File(""));

}
