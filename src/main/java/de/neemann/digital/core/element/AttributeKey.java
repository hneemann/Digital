package de.neemann.digital.core.element;

import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.lang.Lang;

/**
 * @author hneemann
 */
public class AttributeKey<VALUE> {

    public static final AttributeKey<Integer> Bits = new AttributeKey<>("Bits", Lang.get("key_dataBits"), 1);
    public static final AttributeKey<Integer> InputCount = new AttributeKey<>("Inputs", Lang.get("key_numberInputs"), 2);
    public static final AttributeKey<String> Label = new AttributeKey<>("Label", Lang.get("key_label"), "");
    public static final AttributeKey<Integer> Value = new AttributeKey<>("Value", Lang.get("key_value"), 1);
    public static final AttributeKey<Integer> Default = new AttributeKey<>("Default", Lang.get("key_default"), 0);
    public static final AttributeKey<java.awt.Color> Color = new AttributeKey<>("Color", Lang.get("key_color"), java.awt.Color.RED);
    public static final AttributeKey<String> InputSplit = new AttributeKey<>("Input Splitting", Lang.get("key_inputSplitting"), "");
    public static final AttributeKey<String> OutputSplit = new AttributeKey<>("Output Splitting", Lang.get("key_outputSplitting"), "");
    public static final AttributeKey<Integer> Frequency = new AttributeKey<>("Frequency", Lang.get("key_frequency"), 1);
    public static final AttributeKey<Integer> SelectorBits = new AttributeKey<>("Selector Bits", Lang.get("key_selectorBits"), 1);
    public static final AttributeKey<Integer> AddrBits = new AttributeKey<>("Addr Bits", Lang.get("key_addrBits"), InputCount.getDefault());
    public static final AttributeKey<Boolean> Signed = new AttributeKey<>("Signed", Lang.get("key_signed"), false);
    public static final AttributeKey<DataField> Data = new AttributeKey<>("Data", Lang.get("key_data"), DataField.DEFAULT);
    public static final AttributeKey<Boolean> FlipSelPositon = new AttributeKey<>("flipSelPos", Lang.get("key_flipSelPos"), false);
    public static final AttributeKey<Rotation> Rotate = new AttributeKey<>("rotation", Lang.get("key_rotation"), new Rotation(0));
    public static final AttributeKey<Integer> Width = new AttributeKey<>("Width", Lang.get("key_width"), 3);
    public static final AttributeKey<Integer> TermWidth = new AttributeKey<>("termWidth", Lang.get("key_termWidth"), 50);
    public static final AttributeKey<Integer> TermHeight = new AttributeKey<>("termHeight", Lang.get("key_termHeight"), 25);
    public static final AttributeKey<Integer> Cycles = new AttributeKey<>("Cycles", Lang.get("key_cycles"), 100000);
    public static final AttributeKey<Boolean> ValueIsProbe = new AttributeKey<>("valueIsProbe", Lang.get("key_valueIsProbe"), false);
    public static final AttributeKey<Boolean> ShowList = new AttributeKey<>("showList", Lang.get("key_showList"), false);


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

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public VALUE getDefault() {
        return def;
    }

    public Class getValueClass() {
        return def.getClass();
    }

    @Override
    public String toString() {
        return name;
    }

}
