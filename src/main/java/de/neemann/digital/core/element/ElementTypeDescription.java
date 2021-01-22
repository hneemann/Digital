/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.element;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * One instance for an element, so there is only one ElementTypeDescription for an AND.
 * Regardless of how many of these elements are used in the circuit.
 * It has the possibility to create a concrete element by using the given factory
 */
public class ElementTypeDescription {
    private static final String PINSEPARATOR = "_pin_";
    private final String name;
    private final String langKey;
    private ClassLoader classLoader;
    private String shortName;
    private ElementFactory elementFactory;
    private final PinDescriptions inputPins;
    private final ArrayList<Key> attributeList;
    private boolean supportsHDL;

    /**
     * Creates a new ElementTypeDescription
     *
     * @param clazz     the elements class
     * @param inputPins names of the input signals
     */
    public ElementTypeDescription(Class<? extends Element> clazz, PinDescription... inputPins) {
        this(clazz.getSimpleName(), clazz, inputPins);
    }

    /**
     * Creates a new ElementTypeDescription
     *
     * @param name      name of this element
     * @param clazz     the elements class
     * @param inputPins names of the input signals
     */
    public ElementTypeDescription(String name, Class<? extends Element> clazz, PinDescription... inputPins) {
        this(name, attributes -> {
            try {
                Constructor<?> constructor = clazz.getConstructor(ElementAttributes.class);
                return (Element) constructor.newInstance(attributes);
            } catch (Exception e) {
                throw new RuntimeException(Lang.get("err_couldNotCreateElement_N", name), e);
            }
        }, inputPins);
        classLoader = clazz.getClassLoader();
    }

    /**
     * Creates a new ElementTypeDescription
     *
     * @param name           name of this element
     * @param elementFactory factory used to create the element
     * @param inputPins      names of the input signals
     */
    public ElementTypeDescription(String name, ElementFactory elementFactory, PinDescription... inputPins) {
        this.name = name;
        langKey = "elem_" + name;
        this.elementFactory = elementFactory;
        this.inputPins = new PinDescriptions(inputPins).setLangKey(getPinLangKey());
        for (PinDescription p : inputPins)
            if (p.getDirection() != PinDescription.Direction.input)
                throw new RuntimeException("pin direction error");
        attributeList = new ArrayList<>();
    }

    /**
     * Sets the factory to create elements.
     *
     * @param elementFactory the factory
     */
    public void setElementFactory(ElementFactory elementFactory) {
        this.elementFactory = elementFactory;
    }

    /**
     * Returns a short name which should be used to draw on the shape.
     * If not set, the elements name is used instead.
     *
     * @return the shortname
     */
    public String getShortName() {
        if (shortName == null) {
            String s = Lang.getNull(langKey + "_short");
            if (s != null)
                return s;
            return getTranslatedName();
        } else
            return shortName;
    }

    /**
     * @return the language key pin prefix
     */
    public String getPinLangKey() {
        return langKey + PINSEPARATOR;
    }

    /**
     * @return returns the translated element name
     */
    public String getTranslatedName() {
        String n = Lang.getNull(langKey);
        if (n != null) return n;
        else return name;
    }

    /**
     * Sets a short name which should be used to draw it on the shape.
     *
     * @param shortName the short name
     * @return this for call chaining
     */
    public ElementTypeDescription setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    /**
     * Returns a description of this element.
     * If no description is set, the name is returned
     *
     * @param elementAttributes the elements attributes
     * @return the description
     */
    public String getDescription(ElementAttributes elementAttributes) {
        String d = Lang.getNull(langKey + "_tt");
        if (d == null) {
            d = getTranslatedName();
        }
        if (supportsHDL)
            d += " " + Lang.get("msg_supportsHDL");
        return d;
    }

    /**
     * Adds an attribute to this Part.
     * Should be one of the constants in AttributeKey.
     *
     * @param key     the key
     * @param <VALUE> type of the value beloging to the key
     * @return this for call chaining
     * @see Keys
     */
    public <VALUE> ElementTypeDescription addAttribute(Key<VALUE> key) {
        attributeList.add(key);
        if (key == Keys.INT_FORMAT)
            attributeList.add(Keys.FIXED_POINT);
        return this;
    }

    /**
     * Used to flag this elements as supporting hdl export
     *
     * @return this for chained calls
     */
    public ElementTypeDescription supportsHDL() {
        supportsHDL = true;
        return this;
    }

    /**
     * @return true if the element supports export to HDL.
     */
    public boolean isSupportsHDL() {
        return supportsHDL;
    }

    /**
     * Returns the list of attributes which are used by this element.
     *
     * @return the list of attributes
     */
    public ArrayList<Key> getAttributeList() {
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
     * Returns the description of the inputs which are needed by this element.
     * If you need a list of outputs names you can create a element using <code>createElement()</code>
     * and request the outputs by calling the elements <code>getOutputs()</code> method.
     * The you get an array of <code>ObservableName</code>s, and <code>ObservableName</code> has a
     * field <code>name</code>. Or call the getOutputDescription method.
     *
     * @param elementAttributes the elements attributes
     * @return the list of input descriptions
     * @throws NodeException NodeException
     */
    public PinDescriptions getInputDescription(ElementAttributes elementAttributes) throws NodeException {
        return inputPins;
    }

    /**
     * Returns the output pin descriptions of this element.
     *
     * @param elementAttributes the elements attributes
     * @return the list of input descriptions
     * @throws PinException PinException
     */
    public PinDescriptions getOutputDescriptions(ElementAttributes elementAttributes) throws PinException {
        return new PinDescriptions(elementFactory.create(elementAttributes).getOutputs());
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

    /**
     * Returns true if this description contains the given key
     *
     * @param key the key
     * @return true if this description contains the given key
     */
    public boolean hasAttribute(Key key) {
        return attributeList.contains(key);
    }

    /**
     * Returns the input with the given number or null if it does not exist
     *
     * @param i input number
     * @return the inputs description
     */
    public PinDescription getInput(int i) {
        if (i >= inputPins.size())
            return null;
        else
            return inputPins.get(i);
    }

    /**
     * @return the class loader, the component is loaded from. Maybe null.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
