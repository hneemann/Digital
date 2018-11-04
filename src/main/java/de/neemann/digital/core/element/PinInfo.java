/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.element;

import de.neemann.digital.lang.Lang;

/**
 * A simple pin
 */
public class PinInfo implements PinDescription {

    /**
     * Helper to create an input
     *
     * @param name the pins name
     * @return the new input
     */
    public static PinInfo input(String name) {
        return new PinInfo(name, null, Direction.input);
    }

    /**
     * Helper to create an input
     *
     * @param name        the pins name
     * @param description the pins description
     * @return the new input
     */
    public static PinInfo input(String name, String description) {
        return new PinInfo(name, description, Direction.input);
    }

    /**
     * Helper to create an input
     *
     * @param name the pins name
     * @return the new output
     */
    public static PinInfo output(String name) {
        return new PinInfo(name, null, Direction.output);
    }

    private final String description;
    private final String name;
    private String langKey;
    private Direction direction;
    private PullResistor pullResistor;
    private String pinNumber;
    private boolean isClock; // Is used only to draw the small triangle in front of the pins label.
    private boolean isSwitchPin;

    /**
     * Creates a copy of the given {@link PinDescription}
     *
     * @param description the description to copy
     */
    public PinInfo(PinDescription description) {
        this(description.getName(), description.getDescription(), description.getDirection(), description.getPullResistor());
        this.pinNumber = description.getPinNumber();
        this.isClock=description.isClock();
        this.isSwitchPin=description.isSwitchPin();
    }

    /**
     * Creates a new pin
     *
     * @param name        the pins name
     * @param description the pins description
     * @param direction   the pins direction
     */
    public PinInfo(String name, String description, Direction direction) {
        this(name, description, direction, PullResistor.none);
    }

    /**
     * Creates a new pin
     *
     * @param name         the pins name
     * @param description  the pins description
     * @param direction    the pins direction
     * @param pullResistor the connected pullResistor
     */
    public PinInfo(String name, String description, Direction direction, PullResistor pullResistor) {
        this.description = description;
        this.name = name;
        this.direction = direction;
        this.pullResistor = pullResistor;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        if (description != null)
            return description;

        if (langKey != null) {
            String d = Lang.getNull(langKey);
            if (d != null) return d;
        }

        return name;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public PullResistor getPullResistor() {
        return pullResistor;
    }

    @Override
    public String getPinNumber() {
        return pinNumber;
    }

    /**
     * Sethe the pin number
     *
     * @param pinNumber the pin number
     * @return this for chained calls
     */
    public PinInfo setPinNumber(String pinNumber) {
        this.pinNumber = pinNumber;
        return this;
    }

    /**
     * Sets the pull resistor config
     *
     * @param pullResistor the pull resistor config
     * @return this for chained calls
     */
    public PinInfo setPullResistor(PullResistor pullResistor) {
        this.pullResistor = pullResistor;
        return this;
    }

    /**
     * Sets the language key for this pin.
     *
     * @param key the key
     */
    void setLangKey(String key) {
        this.langKey = key + name;
    }

    @Override
    public boolean isClock() {
        return isClock;
    }

    /**
     * Sets the clock flag
     *
     * @return this for chained calls
     */
    public PinInfo setClock() {
        isClock = true;
        return this;
    }

    @Override
    public boolean isSwitchPin() {
        return isSwitchPin;
    }
}
