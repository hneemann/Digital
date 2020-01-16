package de.neemann.digital.plugin;

import de.neemann.digital.core.IntFormat;
import de.neemann.digital.core.element.Key;

/**
 * Define the key constants for plugin
 */
public class PluginKeys {
    private PluginKeys() {}

    /**
     * Set the mode to normal
     */
    public static final Key<Boolean> IS_NORMAL
            = new Key<>("isNormal", true)
            .setName("Normal")
            .setSecondary();


    /**
     * Set the mode to read_before_write
     */
    public static final Key<Boolean> IS_READ_BEFORE_WRITE
            = new Key<>("isReadBeforeWrite", true)
            .setName("Read before write")
            .setSecondary();

    /**
     * Set the mode to write_through
     */
    public static final Key<Boolean> IS_WRITE_THROUGH
            = new Key<>("isWriteThrough", false)
            .setName("Write through")
            .setSecondary();

    /**
     *  设置是否有输出锁存
     */
    public static final Key<Boolean> WITH_OUTPUT_REG
            = new Key<>("withOutputReg", false)
            .setName("With output reg")
            .setSecondary();

    /**
     * Output format for numbers
     */
    public static final Key<IntFormat> INT_FORMAT
            = new Key.KeyEnum<>("intFormat", IntFormat.def, IntFormat.values())
            .setSecondary();

    /*
     * todo: PLL
     */
}
