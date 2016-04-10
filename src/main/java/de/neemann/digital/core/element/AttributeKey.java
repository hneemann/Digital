package de.neemann.digital.core.element;

import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.lang.Lang;

/**
 * A key for a single attribute
 *
 * @param <VALUE> the type of the value
 * @author hneemann
 */
public class AttributeKey<VALUE> {

    //CHECKSTYLE.OFF: ConstantName
    public static final AttributeKey<Integer> Bits = new AttributeKey<>("Bits", Lang.get("key_dataBits"), 1);
    public static final AttributeKey<Integer> InputCount = new AttributeKey<>("Inputs", Lang.get("key_numberInputs"), 2);
    public static final AttributeKey<String> Label = new AttributeKey<>("Label", Lang.get("key_label"), "");
    public static final AttributeKey<Integer> Value = new AttributeKey<>("Value", Lang.get("key_value"), 1);
    public static final AttributeKey<Integer> Default = new AttributeKey<>("Default", Lang.get("key_default"), 0);
    public static final AttributeKey<java.awt.Color> Color = new AttributeKey<>("Color", Lang.get("key_color"), java.awt.Color.RED);
    public static final AttributeKey<String> InputSplit = new AttributeKey<>("Input Splitting", Lang.get("key_inputSplitting"), "");
    public static final AttributeKey<String> OutputSplit = new AttributeKey<>("Output Splitting", Lang.get("key_outputSplitting"), "");
    public static final AttributeKey<Integer> Frequency = new AttributeKeyInteger("Frequency", Lang.get("key_frequency"), 1).setComboBoxValues(new Integer[]{1, 2, 5, 10, 20, 50, 100, 200, 500});
    public static final AttributeKey<Integer> SelectorBits = new AttributeKey<>("Selector Bits", Lang.get("key_selectorBits"), 1);
    public static final AttributeKey<Integer> AddrBits = new AttributeKey<>("Addr Bits", Lang.get("key_addrBits"), InputCount.getDefault());
    public static final AttributeKey<Boolean> Signed = new AttributeKey<>("Signed", Lang.get("key_signed"), false);
    public static final AttributeKey<DataField> Data = new AttributeKey<>("Data", Lang.get("key_data"), DataField.DEFAULT);
    public static final AttributeKey<Boolean> FlipSelPositon = new AttributeKey<>("flipSelPos", Lang.get("key_flipSelPos"), false);
    public static final AttributeKey<Rotation> Rotate = new AttributeKey<>("rotation", Lang.get("key_rotation"), new Rotation(0));
    public static final AttributeKey<Integer> Width = new AttributeKey<>("Width", Lang.get("key_width"), 3);
    public static final AttributeKey<Integer> TermWidth = new AttributeKey<>("termWidth", Lang.get("key_termWidth"), 50);
    public static final AttributeKey<Integer> TermHeight = new AttributeKey<>("termHeight", Lang.get("key_termHeight"), 25);
    public static final AttributeKey<Integer> Cycles = new AttributeKeyInteger("Cycles", Lang.get("key_cycles"), 100000).setComboBoxValues(new Integer[]{1000, 10000, 100000, 1000000});
    public static final AttributeKey<Boolean> ValueIsProbe = new AttributeKey<>("valueIsProbe", Lang.get("key_valueIsProbe"), false);
    public static final AttributeKey<Boolean> ShowListing = new AttributeKey<>("showList", Lang.get("key_showListing"), false);

    public static final AttributeKey<Boolean> ShowDataTable = new AttributeKey<>("showDataTable", Lang.get("key_showDataTable"), false);
    public static final AttributeKey<Boolean> ShowDataGraph = new AttributeKey<>("showDataGraph", Lang.get("key_showDataGraph"), false);
    public static final AttributeKey<Boolean> StartTimer = new AttributeKey<>("startTimer", Lang.get("key_startClock"), false);
    public static final AttributeKey<Boolean> MicroStep = new AttributeKey<>("microStep", Lang.get("key_microStep"), false);
    //CHECKSTYLE.ON: ConstantName

    private final String key;
    private final VALUE def;
    private final String name;

    private AttributeKey(String key, String name, VALUE def) {
        this.key = key;
        this.name = name;
        if (def == null)
            throw new NullPointerException();
        this.def = def;
    }

    /**
     * Returns the attributes key
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the attributes display name
     *
     * @return thr name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the default value of this key
     */
    public VALUE getDefault() {
        return def;
    }

    /**
     * @return The values class
     */
    public Class getValueClass() {
        return def.getClass();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * A integer attribute.
     * Stores additional combo box values
     */
    public static final class AttributeKeyInteger extends AttributeKey<Integer> {
        private Integer[] values;

        private AttributeKeyInteger(String key, String name, Integer def) {
            super(key, name, def);
        }

        private AttributeKeyInteger setComboBoxValues(Integer[] values) {
            this.values = values;
            return this;
        }

        /**
         * @return the values to show in the combo box
         */
        public Integer[] getComboBoxValues() {
            return values;
        }
    }

}
