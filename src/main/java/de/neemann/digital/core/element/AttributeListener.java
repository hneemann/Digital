package de.neemann.digital.core.element;

/**
 * Interface to implement a AttributeListener
 *
 * @author hneemann
 */
public interface AttributeListener {
    /**
     * Is called if an attribute changes
     *
     * @param key the key which value has changed
     */
    void attributeChanged(AttributeKey key);
}
