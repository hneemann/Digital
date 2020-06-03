/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.draw.graphics.VectorFloat;

/**
 * A movable element.
 *
 * @param <A> the type of the implementing class
 */
public class Movable<A extends Movable<?>> implements MouseMovable {

    enum Property {POS, REMOVED, CONDITION, NAME, NUMBER, MOUSEPOS, VALUES, INITIAL, ADDED, INITIAL_ANGLE}

    private static final float MASS = 50f;
    private static final float FRICTION = 0.8f;
    private static final float MAX_FORCE = 100000f;
    private static final float MAX_FORCE_CHECK = (float) (MAX_FORCE / Math.sqrt(2));

    private String values = "";
    private VectorFloat position;
    private transient VectorFloat speed;
    private transient VectorFloat force;
    private transient FSM fsm;
    private transient Property lastPosProp;

    /**
     * Creates a new instance
     */
    public Movable() {
        force = new VectorFloat(0, 0);
        speed = new VectorFloat(0, 0);
        position = new VectorFloat(0, 0);
    }

    /**
     * Sets the position by mouse movement
     *
     * @param position the position
     */
    public void setPosDragging(VectorFloat position) {
        setPos(position, Property.MOUSEPOS);
    }

    /**
     * Sets the position
     *
     * @param position the position
     */
    public void setPos(VectorFloat position) {
        setPos(position, Property.POS);
    }

    private void setPos(VectorFloat position, Property prop) {
        if (!this.position.equals(position) || lastPosProp != prop) {
            this.position = position;
            lastPosProp = prop;
            wasModified(prop);
        }
    }

    void wasModified(Property prop) {
        if (fsm != null)
            fsm.wasModified(this, prop);
    }

    /**
     * Adds the given value to the force
     *
     * @param df the force to add
     */
    void addToForce(VectorFloat df) {
        if (force == null)
            force = df;
        else
            force = force.add(df);
    }

    /**
     * Applies a repulsive force which decreases with the square of the distance.
     *
     * @param pos   the position of the causer of the force
     * @param reach the reach of the force
     */
    void addRepulsive(VectorFloat pos, float reach) {
        final VectorFloat dif = position.sub(pos);
        float dist = dif.len();
        if (dist == 0) {
            addToForce(new VectorFloat((float) Math.random() - 0.5f, (float) Math.random() - 0.5f).mul(2));
        } else {
            float f = reach * reach / (dist * dist) / 2;
            addToForce(dif.norm().mul(f));
        }
    }

    /**
     * Applies a repulsive force which decreases linear with the the distance.
     *
     * @param pos   the position of the causer of the force
     * @param reach the reach of the force
     */
    public void addRepulsiveInv(VectorFloat pos, float reach) {
        final VectorFloat dif = position.sub(pos);
        float dist = dif.len();
        if (dist == 0) {
            addToForce(new VectorFloat((float) Math.random() - 0.5f, (float) Math.random() - 0.5f).mul(2));
        } else {
            float f = reach / dist / 2;
            if (f > 100)
                f = 100;
            addToForce(dif.norm().mul(f));
        }
    }

    /**
     * Adds an attractive force
     *
     * @param target the targe
     * @param scale  a scaling factor
     */
    void addAttractiveTo(VectorFloat target, float scale) {
        addToForce(target.sub(position).mul(scale));
    }

    /**
     * @return the force
     */
    VectorFloat getForce() {
        if (force == null)
            resetForce();
        return force;
    }

    /**
     * Sets the force to zero
     */
    void resetForce() {
        this.force = new VectorFloat(0, 0);
    }

    /**
     * @return the position
     */
    public VectorFloat getPos() {
        return position;
    }

    /**
     * Moves the element
     *
     * @param dt the time step in ms
     */
    public void move(int dt) {
        if (Math.abs(force.getXFloat()) > MAX_FORCE_CHECK || Math.abs(force.getYFloat()) > MAX_FORCE_CHECK) {
            double len = force.len();
            if (len > MAX_FORCE)
                force = force.norm().mul(MAX_FORCE);
        }
        if (speed == null)
            speed = force.mul(dt / MASS);
        else
            speed = speed.add(force.mul(dt / MASS));
        setPos(position.add(speed.mul(dt / 1000f)));
        speed = speed.mul(FRICTION);
    }

    void setFSM(FSM fsm) {
        this.fsm = fsm;
    }

    FSM getFsm() {
        return fsm;
    }

    /**
     * Sets the values to define as a comma separated string with assignments: 'A=0,B=1'
     *
     * @param values the assignments
     * @return this for chained calls
     */
    public A setValues(String values) {
        if (!this.values.equals(values)) {
            this.values = values;
            wasModified(Property.VALUES);
        }
        return (A) this;
    }

    /**
     * @return the state value map
     */
    public String getValues() {
        return values;
    }
}
