/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.draw.graphics.*;

import java.util.List;
import java.util.TreeMap;

/**
 * Represents a state
 */
public class State extends Movable {
    private static final int RASTER = 60;

    private static final int RAD = 70;
    private static final float REACH = 2000;
    private static final int INIT_RADIUS = 20;

    private int number = -1;
    private String name;
    private int radius;
    private String values = "";

    private transient TreeMap<String, Integer> valueMap;

    /**
     * Creates a new state
     *
     * @param name the name of the state
     */
    public State(String name) {
        super();
        this.name = name;
        this.radius = RAD;
    }

    /**
     * Sets the values to define as a comma separated string with assignments: 'A=0,B=1'
     *
     * @param values the assignments
     * @return this for chained calls
     */
    public State setValues(String values) {
        if (!this.values.equals(values)) {
            this.values = values;
            valueMap = null;
            wasModified();
        }
        return this;
    }

    /**
     * @return the state value map
     */
    public String getValues() {
        return values;
    }

    /**
     * @return the state value map
     * @throws FiniteStateMachineException FiniteStateMachineException
     */
    public TreeMap<String, Integer> getValueMap() throws FiniteStateMachineException {
        if (valueMap == null)
            valueMap = new ValueParser(values).parse();
        return valueMap;
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
            wasModified();
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
     * Calculates the repolsive forces
     *
     * @param states the states to take into account
     */
    public void calcExpansionForce(List<State> states) {
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
        if (isInitialState()) {
            VectorInterface rad = new Vector(INIT_RADIUS, INIT_RADIUS);
            gr.drawCircle(getPos().sub(rad), getPos().add(rad), Style.FILLED);
        } else {
            VectorInterface rad = new Vector(radius, radius);
            gr.drawCircle(getPos().sub(rad), getPos().add(rad), Style.NORMAL);
            if (number == 0) {
                VectorInterface rad2 = new Vector(radius - Style.MAXLINETHICK * 2, radius - Style.MAXLINETHICK * 2);
                gr.drawCircle(getPos().sub(rad2), getPos().add(rad2), Style.THIN);
            }

            Vector delta = new Vector(0, Style.NORMAL.getFontSize());
            VectorFloat pos = getPos().add(delta.mul(-1));

            gr.drawText(pos, pos.add(new Vector(1, 0)), Integer.toString(number), Orientation.CENTERCENTER, Style.NORMAL);
            pos = pos.add(delta);
            gr.drawText(pos, pos.add(new Vector(1, 0)), name, Orientation.CENTERCENTER, Style.NORMAL);

            if (values != null) {
                pos = pos.add(delta);
                gr.drawText(pos, pos.add(new Vector(1, 0)), values, Orientation.CENTERCENTER, Style.NORMAL);
            }
        }
    }

    private boolean isInitialState() {
        return getFsm() != null && getFsm().isInitial(this);
    }

    /**
     * @return the radius of the state
     */
    float getVisualRadius() {
        if (isInitialState())
            return INIT_RADIUS;
        else
            return radius;
    }

    /**
     * @return the radius of the state
     */
    public float getRadius() {
        return radius;
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
            wasModified();
            if (getFsm() != null)
                getFsm().resetInitInitialization();
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

}
