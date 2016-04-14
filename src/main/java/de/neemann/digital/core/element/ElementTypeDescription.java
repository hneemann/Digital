package de.neemann.digital.core.element;

import de.neemann.digital.core.NodeException;

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
    private final PinDescription[] inputPins;
    private final ArrayList<AttributeKey> attributeList;

    /**
     * Creates a new ElementTypeDescription
     *
     * @param clazz      the elements class
     * @param inputPins names of the input signals
     */
    public ElementTypeDescription(Class<? extends Element> clazz, PinDescription... inputPins) {
        this(clazz.getSimpleName(), clazz, inputPins);
    }

    /**
     * Creates a new ElementTypeDescription
     *
     * @param name       name of this element
     * @param clazz      the elements class
     * @param inputPins names of the input signals
     */
    public ElementTypeDescription(String name, Class<? extends Element> clazz, PinDescription... inputPins) {
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
        }, inputPins);
    }

    /**
     * Creates a new ElementTypeDescription
     *
     * @param name           name of this element
     * @param elementFactory factory used to create the element
     * @param inputPins     names of the input signals
     */
    public ElementTypeDescription(String name, ElementFactory elementFactory, PinDescription... inputPins) {
        this.name = name;
        this.shortName = name;
        this.elementFactory = elementFactory;
        this.inputPins = inputPins;
        for (PinDescription p : inputPins)
            if (p.getDirection() != PinDescription.Direction.input)
                throw new RuntimeException("pin direction error");
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
     * @throws NodeException NodeException
     */
    public PinDescription[] getInputNames(ElementAttributes elementAttributes) throws NodeException {
        return inputPins;
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
