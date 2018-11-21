/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.draw.graphics.*;

import java.util.List;

/**
 * Represents a transition
 */
public class Transition extends Movable {
    private static final FormatToExpression FORMAT = FormatToExpression.FORMATTER_UNICODE;
    private static final float EXPANSION_TRANS = 0.001f;

    private final State fromState;
    private final State toState;
    private final Expression condition;

    /**
     * Creates a new transition
     *
     * @param fromState the state to leave
     * @param toState   the state to enter
     * @param condition the condition
     */
    public Transition(State fromState, State toState, Expression condition) {
        super();
        this.fromState = fromState;
        this.toState = toState;
        this.condition = condition;
        initPos();
    }

    /**
     * Calculates the repulsive forces
     *
     * @param states      the states
     * @param transitions the transitions
     */
    public void calcForce(List<State> states, List<Transition> transitions) {
        float preferredLen = Math.max(fromState.getRadius(), toState.getRadius()) * 5;
        calcForce(preferredLen, states, transitions);
    }

    /**
     * Calculates the repulsive forces
     *
     * @param preferredDist the preferred distance
     * @param states        the states
     * @param transitions   the transitions
     */
    public void calcForce(float preferredDist, List<State> states, List<Transition> transitions) {

        VectorFloat dir = fromState.getPos().sub(toState.getPos());
        float len = dir.len();
        float d = len - preferredDist;
        dir = dir.mul(EXPANSION_TRANS * d);
        toState.addToForce(dir);
        fromState.addToForce(dir.mul(-1));

        resetForce();
        VectorFloat center = fromState.getPos().add(toState.getPos()).mul(0.5f);
        addAttractiveTo(center, 1);

        for (State s : states)
            addRepulsive(s.getPos(), 2000);

        for (Transition t : transitions)
            if (t != this)
                addRepulsiveInv(t.getPos(), 1000);

    }

    /**
     * Draws the transition
     *
     * @param gr the Graphic instance to draw to
     */
    public void drawTo(Graphic gr) {
        VectorFloat difFrom = getPos().sub(fromState.getPos()).norm().mul(fromState.getRadius());
        VectorFloat difTo = getPos().sub(toState.getPos()).norm().mul(toState.getRadius());

        final VectorFloat start = fromState.getPos().add(difFrom);
        final VectorFloat end = toState.getPos().add(difTo);

        Polygon p = new Polygon(false)
                .add(start)
                .add(getPos(), getPos(), end);
        final Style arrowStyle = Style.SHAPE_PIN;
        gr.drawPolygon(p, arrowStyle);

//        gr.drawLine(start, getPos(), Style.THIN);
//        gr.drawLine(getPos(), end, Style.THIN);

        // arrow
        VectorFloat lot = new VectorFloat(difTo.getYFloat(), -difTo.getXFloat()).mul(0.5f);
        gr.drawLine(end, end.add(difTo.add(lot).mul(0.2f)), arrowStyle);
        gr.drawLine(end, end.add(difTo.sub(lot).mul(0.2f)), arrowStyle);
        if (condition != null) {
            String format;
            try {
                format = FORMAT.format(condition);
            } catch (FormatterException e) {
                format = "error";
            }
            gr.drawText(getPos(), getPos().add(new Vector(1, 0)), format, Orientation.CENTERCENTER, Style.NORMAL);
        }
    }

    /**
     * Initializes the position of the transition
     */
    public void initPos() {
        setPos(fromState.getPos().add(toState.getPos()).mul(0.5f)
                .add(new VectorFloat((float) Math.random() - 0.5f, (float) Math.random() - 0.5f).mul(30)));
    }
}
