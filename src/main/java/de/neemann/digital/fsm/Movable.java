/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.draw.graphics.VectorFloat;

/**
 * A movable element.
 */
public class Movable {
    private VectorFloat position;
    private VectorFloat speed;
    private VectorFloat force;

    /**
     * Creates a new instance
     */
    public Movable() {
        force = new VectorFloat(0, 0);
        speed = new VectorFloat(0, 0);
        position = new VectorFloat((float) Math.random() - 0.5f, (float) Math.random() - 0.5f).mul(100);
    }

    /**
     * Sets the position
     *
     * @param position the position
     */
    public void setPos(VectorFloat position) {
        this.position = position;
    }

    /**
     * Adds the given value to the force
     *
     * @param df the force to add
     */
    public void addToForce(VectorFloat df) {
        force = force.add(df);
    }

    /**
     * Applies a repulsive force which decreases with the square of the distance.
     *
     * @param pos   the position of the causer of the force
     * @param reach the reach of the force
     */
    public void addRepulsive(VectorFloat pos, float reach) {
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
    public void addAttractiveTo(VectorFloat target, float scale) {
        addToForce(target.sub(position).mul(scale));
    }

    /**
     * @return the force
     */
    public VectorFloat getForce() {
        return force;
    }

    /**
     * Sets the force to zero
     */
    public void resetForce() {
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
     * @param dt the time step
     */
    public void move(int dt) {
        speed = speed.add(force.mul(dt / 200f));
        setPos(position.add(speed.mul(dt / 1000f)));
        speed = speed.mul(0.7f);
    }

}
