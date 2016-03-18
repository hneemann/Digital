package de.neemann.digital.gui.components;

/**
 * @author hneemann
 */
public interface Editor<T> {

    String toText(T value);

    T toValue(String text);

}
