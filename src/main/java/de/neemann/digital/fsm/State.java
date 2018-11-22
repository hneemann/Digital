/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.draw.graphics.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a state
 */
public class State extends Movable {
    private static final int RAD = 70;
    private static final float REACH = 2000;

    private int number = -1;
    private String name;
    private int radius;
    private TreeMap<String, Long> values;

    /**
     * Creates a new state
     *
     * @param name the name of the state
     */
    public State(String name) {
        super();
        this.name = name;
        this.radius = RAD;
        values = new TreeMap<>();
    }

    /**
     * Adds a outputvalue to the state
     *
     * @param name the name
     * @param val  the value
     * @return this for chained calls
     */
    public State val(String name, long val) {
        values.put(name, val);
        return this;
    }

    /**
     * @return the name of the state
     */
    public String getName() {
        return name;
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
        VectorInterface rad = new Vector(RAD, RAD);
        gr.drawCircle(getPos().sub(rad), getPos().add(rad), Style.NORMAL);
        if (number == 0) {
            VectorInterface rad2 = new Vector(RAD - Style.MAXLINETHICK * 2, RAD - Style.MAXLINETHICK * 2);
            gr.drawCircle(getPos().sub(rad2), getPos().add(rad2), Style.THIN);
        }

        Vector delta = new Vector(0, Style.NORMAL.getFontSize());
        VectorFloat pos = getPos().add(delta.mul(-1));

        gr.drawText(pos, pos.add(new Vector(1, 0)), Integer.toString(number), Orientation.CENTERCENTER, Style.NORMAL);
        pos = pos.add(delta);
        gr.drawText(pos, pos.add(new Vector(1, 0)), name, Orientation.CENTERCENTER, Style.NORMAL);

        for (Map.Entry<String, Long> v : values.entrySet()) {
            pos = pos.add(delta);
            String str = v.getKey() + "->" + v.getValue();
            gr.drawText(pos, pos.add(new Vector(1, 0)), str, Orientation.CENTERCENTER, Style.NORMAL);
        }

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
        this.number = number;
        return this;
    }

    /**
     * @return the number of the state
     */
    public int getNumber() {
        return number;
    }

    /**
     * @return the state value map
     */
    public TreeMap<String, Long> getValues() {
        return values;
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
}
