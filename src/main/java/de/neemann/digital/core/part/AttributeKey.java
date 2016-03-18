package de.neemann.digital.core.part;

/**
 * @author hneemann
 */
public class AttributeKey<VALUE> {

    public static final AttributeKey<Integer> Bits = new AttributeKey<>("Bits", 1);
    public static final AttributeKey<Integer> InputCount = new AttributeKey<>("Inputs", 2);

    private final String name;
    private final VALUE def;

    public AttributeKey(String name, VALUE def) {
        this.name = name;
        this.def = def;
    }

    public String getName() {
        return name;
    }

    public VALUE getDefault() {
        return def;
    }
}
