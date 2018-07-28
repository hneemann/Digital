/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.element;

import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.core.IntFormat;
import de.neemann.digital.core.arithmetic.BarrelShifterMode;
import de.neemann.digital.core.arithmetic.LeftRightFormat;
import de.neemann.digital.core.extern.Application;
import de.neemann.digital.core.io.InValue;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.rom.ROMManger;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.gui.Screen;
import de.neemann.gui.language.Language;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;

/**
 * Collection of key constants
 */
public final class Keys {

    private static final class InstanceHolder {
        private static final HashMap<String, Key> INSTANCE = createMap();

        private static HashMap<String, Key> createMap() {
            HashMap<String, Key> map = new HashMap<>();
            for (Field k : Keys.class.getDeclaredFields()) {
                if (Modifier.isStatic(k.getModifiers()) && Key.class.isAssignableFrom(k.getType())) {
                    try {
                        Key key = (Key) k.get(null);
                        map.put(key.getKey(), key);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("error accessing the Keys");
                    }
                }
            }
            return map;
        }
    }

    /**
     * Returns the key of the given name.
     * If key does not exist, null is returned.
     *
     * @param name the name of the key
     * @return the key or null
     */
    public static Key getKeyByName(String name) {
        return InstanceHolder.INSTANCE.get(name);
    }

    /**
     * @return all available keys
     */
    public static Iterable<Key> getKeys() {
        return InstanceHolder.INSTANCE.values();
    }


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
     * The counter max value
     */
    public static final Key.KeyInteger MAX_VALUE
            = new Key.KeyInteger("maxValue", 0)
            .setMin(0);


    /**
     * the delay time used by the delay component
     */
    public static final Key.KeyInteger DELAY_TIME
            = new Key.KeyInteger("delayTime", 1)
            .setComboBoxValues(new Integer[]{1, 2, 3, 4, 5})
            .setMin(1)
            .setMax(20);

    /**
     * the timer delay time
     */
    public static final Key.KeyInteger MONOFLOP_DELAY
            = new Key.KeyInteger("timerDelay", 1)
            .setMin(1);

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
     * text orientation
     */
    public static final Key<Orientation> TEXT_ORIENTATION
            = new Key.KeyEnum<>("textOrientation", Orientation.LEFTTOP, Orientation.values()).setSecondary();


    /**
     * The size of a LED
     */
    public static final Key<Integer> LED_SIZE
            = new Key.KeyInteger("Size", 1)
            .setComboBoxValues(new Integer[]{0, 1, 2, 3, 4, 5})
            .setMin(0)
            .allowGroupEdit()
            .setSecondary();

    /**
     * The value of constants
     */
    public static final Key<Long> VALUE
            = new Key<>("Value", 1L).allowGroupEdit();

    /**
     * The default value of elements
     */
    public static final Key<Integer> DEFAULT
            = new Key<>("Default", 0).allowGroupEdit().setSecondary();

    /**
     * The default value of inputs
     */
    public static final Key<InValue> INPUT_DEFAULT
            = new Key<>("InDefault", new InValue(0)).allowGroupEdit().setSecondary();

    /**
     * The default value of the dip switch
     */
    public static final Key<Boolean> DIP_DEFAULT
            = new Key<>("dipDefault", false).allowGroupEdit().setSecondary();


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
     * The splitter spreading
     */
    public static final Key<Integer> SPLITTER_SPREADING
            = new Key.KeyInteger("splitterSpreading", 1)
            .setComboBoxValues(new Integer[]{1, 2, 3, 4})
            .setMin(1)
            .setMax(10)
            .setSecondary();


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
            = new Key<>("rotation", new Rotation(0)).allowGroupEdit().setSecondary();

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
            = new Key<>("valueIsProbe", false).allowGroupEdit().setSecondary();

    /**
     * flag to set a ROM as program memory
     */
    public static final Key<Boolean> IS_PROGRAM_MEMORY
            = new Key<>("isProgramMemory", false).setSecondary();

    /**
     * flag to set a ROM as program memory
     */
    public static final Key<Boolean> IS_PROGRAM_COUNTER
            = new Key<>("isProgramCounter", false).setSecondary();

    /**
     * flag to enable the ROMs auto load function
     */
    public static final Key<Boolean> AUTO_RELOAD_ROM
            = new Key<>("autoReload", false).setSecondary();

    /**
     * flag to show the data table window
     */
    public static final Key<Boolean> SHOW_DATA_TABLE
            = new Key<>("showDataTable", false).setSecondary();

    /**
     * flag to show the data graph window
     */
    public static final Key<Boolean> SHOW_DATA_GRAPH
            = new Key<>("showDataGraph", false).setSecondary();

    /**
     * flag to show the data graph window in single gate mode
     */
    public static final Key<Boolean> SHOW_DATA_GRAPH_MICRO
            = new Key<>("showDataGraphMicro", false).setSecondary();

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
            = new Key<>("isHighZ", false).allowGroupEdit().setSecondary();

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
     * enables the MAC mouse mode
     */
    public static final Key<Boolean> SETTINGS_MAC_MOUSE
            = new Key<>("macMouse", Screen.isMac());

    /**
     * output format for numbers
     */
    public static final Key<IntFormat> INT_FORMAT
            = new Key.KeyEnum<>("intFormat", IntFormat.def, IntFormat.values()).setSecondary();

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
     * Number of poles in the double throw relay
     */
    public static final Key<Integer> POLES
            = new Key.KeyInteger("poles", 1)
            .setComboBoxValues(new Integer[]{1, 2, 3, 4})
            .setMin(1).allowGroupEdit();


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
     * Fitter for the atf15xx
     */
    public static final Key<File> SETTINGS_ATF1502_FITTER
            = new Key.KeyFile("atf1502Fitter", new File("c:/Wincupl/WinCupl/Fitters")).setDirectoryOnly(true).setSecondary();

    /**
     * Flash software for the atf15xx
     */
    public static final Key<File> SETTINGS_ATMISP
            = new Key.KeyFile("ATMISP", new File("c:/ATMISP7/ATMISP.exe")).setSecondary();

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
            new Key<>("pinNumber", "").setSecondary();

    /**
     * true if shape is a dil shape
     */
    public static final Key<Boolean> IS_DIL
            = new Key<>("isDIL", false).setSecondary();
    /**
     * the pin count
     */
    public static final Key<Integer> PINCOUNT =
            new Key.KeyInteger("pinCount", 0)
                    .setMin(0)
                    .setDependsOn(IS_DIL)
                    .setSecondary();


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
            = new Key.KeyFile("libraryPath", ElementLibrary.getLibPath()).setDirectoryOnly(true).setSecondary();

    /**
     * A jar containing custom java components
     */
    public static final Key<File> SETTINGS_JAR_PATH
            = new Key.KeyFile("jarPath", new File("")).setSecondary();

    /**
     * The manager which contains all the roms data
     */
    public static final Key<ROMManger> ROMMANAGER
            = new Key<>("romContent", ROMManger.EMPTY).setSecondary();


    /**
     * The type of the external process
     */
    public static final Key.KeyEnum<Application.Type> APPLICATION_TYPE
            = new Key.KeyEnum<>("applicationType", Application.Type.Generic, Application.Type.values());
    /**
     * The inputs used by the external process
     */
    public static final Key<String> EXTERNAL_INPUTS
            = new Key<>("externalInputs", "in");
    /**
     * The outputs used by the external process
     */
    public static final Key<String> EXTERNAL_OUTPUTS
            = new Key<>("externalOutputs", "out");
    /**
     * The code to be executed by the external process
     */
    public static final Key.LongString EXTERNAL_CODE
            = new Key.LongString("Code").setRows(30).setColumns(80).setLineNumbers(true);

    /**
     * Path to ghdl
     */
    public static final Key<File> SETTINGS_GHDL_PATH
            = new Key.KeyFile("ghdlPath", new File("ghdl")).setSecondary();

    /**
     * Path to iverilog installation directory
     */
    public static final Key<File> SETTINGS_IVERILOG_PATH
            = new Key.KeyFile("iverilogPath", new File("iverilog")).setSecondary();

    /**
     * Avoid component tooltips in the main panel
     */
    public static final Key<Boolean> SETTINGS_NOTOOLTIPS =
            new Key<>("noComponentToolTips", false);

    /**
     * Used to show the default shape also if the circuit defines a DIL.
     */
    public static final Key<Boolean> USE_DEFAULT_SHAPE
            = new Key<Boolean>("useDefShape", false);

    /**
     * Shape used to represent a visual element
     */
    public static final Key<CustomShapeDescription> CUSTOM_SHAPE
            = new Key<>("customShape", CustomShapeDescription.EMPTY).setSecondary();

    /**
     * True if a program is loaded to the simulator at startup
     */
    public static final Key<Boolean> PRELOAD_PROGRAM
            = new Key<Boolean>("preloadProgram", false).setSecondary();

    /**
     * The file to preload as a program at startup
     */
    public static final Key<File> PROGRAM_TO_PRELOAD
            = new Key.KeyFile("preloadProgramFile", new File("")).setSecondary();

}
