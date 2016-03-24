package de.neemann.digital.core.element;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * One instance for an element, so there is only one ElementTypeDescription for an AND.
 * Regardless of how many of these elements are used in the circuit.
 * It has the possibility to create a concrete element by using the given factory
 *
 * @author hneemann
 */
public class ElementTypeDescription {
    private final String name;
    private String shortName;
    private final ElementFactory elementFactory;
    private final String[] inputNames;
    private final ArrayList<AttributeKey> attributeList;

    public ElementTypeDescription(Class<?> clazz, String... inputNames) {
        this(clazz.getSimpleName(), clazz, inputNames);
    }

    public ElementTypeDescription(String name, Class<?> clazz, String... inputNames) {
        this(name, new ElementFactory() {
            @Override
            public Element create(ElementAttributes attributes) {
                try {
                    Constructor<?> constructor = clazz.getConstructor(ElementAttributes.class);
                    return (Element) constructor.newInstance(attributes);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, inputNames);
    }

    public ElementTypeDescription(String name, ElementFactory elementFactory, String... inputNames) {
        this.name = name;
        this.shortName = name;
        this.elementFactory = elementFactory;
        this.inputNames = inputNames;
        attributeList = new ArrayList<>();
    }

    /**
     * Returns a short name which should be used to draw on the shape.
     * If not set, the elements name is used instead.
     *
     * @return the shortname
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Returns a short name which should be used to draw on the shape.
     *
     * @param shortName the short name
     * @return this for call chaining
     */
    public ElementTypeDescription setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    /**
     * Adds an attribute to this Part.
     * Should be one of the constants in AttributeKey.
     *
     * @param key     the key
     * @param <VALUE> type of the value beloging to the key
     * @return this for call chaining
     * @see AttributeKey
     */
    public <VALUE> ElementTypeDescription addAttribute(AttributeKey<VALUE> key) {
        attributeList.add(key);
        return this;
    }

    /**
     * Returns the list of attributes which are used by this element.
     *
     * @return the list of attributes
     */
    public ArrayList<AttributeKey> getAttributeList() {
        return attributeList;
    }

    /**
     * The name of this element.
     * The name needs to be unique.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the names of the inputs which are needed by this element.
     * If you need a list of outputs names you can create a element using <code>createElement()</code>
     * and request the outputs by calling the elements <code>getOutputs()</code> method.
     * The you get an array of <code>ObservableName</code>s, and <code>ObservableName</code> has a
     * field <code>name</code>.
     *
     * @param elementAttributes the elements attributes
     * @return the list of input names
     */
    public String[] getInputNames(ElementAttributes elementAttributes) {
        return inputNames;
    }

    /**
     * Creates a element of this type
     *
     * @param elementAttributes the elements attributes
     * @return the Part instance
     */
    public Element createElement(ElementAttributes elementAttributes) {
        return elementFactory.create(elementAttributes);
    }
}
