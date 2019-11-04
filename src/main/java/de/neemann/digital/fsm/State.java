/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.draw.graphics.*;

import java.util.List;

/**
 * Represents a state
 */
public class State extends Movable<State> {
    /**
     * The default state radius
     */
    static final int DEFAULT_RAD = 70;

    private static final int RASTER = 60;
    private static final float REACH = 2000;

    private int number = -1;
    private String name;
    private int radius;
    private boolean isInitial;

    /**
     * Creates a new state
     *
     * @param name the name of the state
     */
    public State(String name) {
        super();
        this.name = name;
        this.radius = DEFAULT_RAD;
    }

    /**
     * @return the name of the state
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        if (!this.name.equals(name)) {
            this.name = name;
            wasModified(Property.NAME);
        }
    }

    /**
     * Sets the position
     *
     * @param position the position
     * @return this for chained calls
     */
    public State setPosition(VectorFloat position) {
        setPos(position);
        return this;
    }

    /**
     * Calculates the repulsive forces
     *
     * @param states the states to take into account
     */
    void calcExpansionForce(List<State> states) {
        resetForce();
        for (State s : states)
            if (s != this)
                addRepulsive(s.getPos(), REACH);
    }


    /**
     * Draws the state
     *
     * @param gr the Graphic instance to draw to
     */
    public void drawTo(Graphic gr) {
        Style style = Style.NORMAL;
        if (getFsm() != null)
            if (getFsm().getActiveState() == number)
                style = Style.HIGHLIGHT;

        if (isInitial)
            style = style.deriveStyle(style.getThickness() * 2, false, style.getColor());

        VectorInterface rad = new Vector(radius, radius);
        gr.drawCircle(getPos().sub(rad), getPos().add(rad), style);

        Vector delta = new Vector(0, Style.NORMAL.getFontSize());
        VectorFloat pos = getPos().add(delta.mul(-1));

        gr.drawText(pos, Integer.toString(number), Orientation.CENTERCENTER, Style.NORMAL);
        pos = pos.add(delta);
        gr.drawText(pos, name, Orientation.CENTERCENTER, Style.NORMAL);

        if (getValues() != null && getValues().length() > 0) {
            pos = pos.add(delta);
            gr.drawText(pos, getValues(), Orientation.CENTERCENTER, Style.INOUT);
        }
    }

    /**
     * @return the radius of the state
     */
    public int getVisualRadius() {
        return radius;
    }

    /**
     * @return the radius of the state
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Sets the radius of the state
     *
     * @param radius the radius
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * Sets the number of the state
     *
     * @param number the number
     * @return this for chained calls
     */
    public State setNumber(int number) {
        if (this.number != number) {
            this.number = number;
            wasModified(Property.NUMBER);
        }
        return this;
    }

    /**
     * @return the number of the state
     */
    public int getNumber() {
        return number;
    }

    /**
     * Returns true if the position matches the state
     *
     * @param pos the position
     * @return true if pos inside of the state
     */
    public boolean matches(Vector pos) {
        return pos.sub(getPos()).len() <= radius;
    }

    @Override
    public String toString() {
        if (name != null && name.length() > 0)
            return name;
        else
            return Integer.toString(number);
    }

    /**
     * Sets state to raster
     *
     * @return this for chained calls
     */
    public State toRaster() {
        setPosition(new VectorFloat(
                Math.round(getPos().getXFloat() / RASTER) * RASTER,
                Math.round(getPos().getYFloat() / RASTER) * RASTER));
        return this;
    }

    /**
     * @return true if this is the initial state
     */
    public boolean isInitial() {
        return isInitial;
    }

    /**
     * Sets this state as the initial state.
     *
     * @param isInitial true is this is the initial state
     */
    public void setInitial(boolean isInitial) {
        if (isInitial)
            if (getFsm() != null)
                getFsm().clearInitial();

        if (this.isInitial != isInitial) {
            this.isInitial = isInitial;
            wasModified(Property.INITIAL);
        }

    }
}
