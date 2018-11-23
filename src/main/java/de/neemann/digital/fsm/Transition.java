/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.parser.ParseException;
import de.neemann.digital.analyse.parser.Parser;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a transition
 */
public class Transition extends Movable {
    private static final float EXPANSION_TRANS = 0.001f;

    private final State fromState;
    private final State toState;
    private String condition;
    private transient Expression conditionExpression;
    private transient boolean isInitial;


    /**
     * Creates a new transition
     *
     * @param fromState the state to leave
     * @param toState   the state to enter
     * @param condition the condition
     */
    public Transition(State fromState, State toState, String condition) {
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
        float preferredDist = 20;
        if (!isInitial)
            preferredDist = Math.max(fromState.getRadius(), toState.getRadius()) * 5;
        calcForce(preferredDist, states, transitions);
    }

    /**
     * Calculates the repulsive forces
     *
     * @param preferredDist the preferred distance
     * @param states        the states
     * @param transitions   the transitions
     */
    public void calcForce(float preferredDist, List<State> states, List<Transition> transitions) {

        if (fromState != toState) {
            VectorFloat dir = fromState.getPos().sub(toState.getPos());
            float len = dir.len();
            float d = len - preferredDist;
            dir = dir.mul(EXPANSION_TRANS * d);
            toState.addToForce(dir);
            fromState.addToForce(dir.mul(-1));
        }

        resetForce();
        VectorFloat center = fromState.getPos().add(toState.getPos()).mul(0.5f);
        addAttractiveTo(center, 1);

        if (!isInitial) {
            for (State s : states)
                addRepulsive(s.getPos(), 2000);

            for (Transition t : transitions)
                if (t != this)
                    addRepulsive(t.getPos(), 800);
        }
    }

    @Override
    public void setPos(VectorFloat position) {
        if (fromState != toState) {
            VectorFloat dist = fromState.getPos().sub(toState.getPos());
            VectorFloat p = position.sub(fromState.getPos());
            VectorFloat n = new VectorFloat(dist.getYFloat(), -dist.getXFloat()).norm();
            float l = p.mul(n);
            super.setPos(fromState.getPos().sub(dist.mul(0.5f)).add(n.mul(l)));
        } else
            super.setPos(position);
    }

    /**
     * Draws the transition
     *
     * @param gr the Graphic instance to draw to
     */
    public void drawTo(Graphic gr) {

        VectorFloat anchorFrom = getPos();
        VectorFloat anchorTo = getPos();

        if (fromState == toState) {
            VectorFloat dif = anchorFrom.sub(fromState.getPos());
            dif = new VectorFloat(dif.getYFloat(), -dif.getXFloat()).mul(0.3f);
            anchorFrom = anchorFrom.add(dif);
            anchorTo = anchorTo.sub(dif);
        }

        VectorFloat difFrom = anchorFrom.sub(fromState.getPos()).norm().mul(fromState.getRadius() + Style.MAXLINETHICK);
        VectorFloat difTo = anchorTo.sub(toState.getPos()).norm().mul(toState.getRadius() + Style.MAXLINETHICK + 2);
        VectorFloat difToTip = anchorTo.sub(toState.getPos()).norm().mul(toState.getRadius() + Style.MAXLINETHICK);

        final VectorFloat start = fromState.getPos().add(difFrom);
        final VectorFloat end = toState.getPos().add(difTo);
        final VectorFloat arrowTip = toState.getPos().add(difToTip);

        Polygon p = new Polygon(false)
                .add(start)
                .add(anchorFrom, anchorTo, end);
        final Style arrowStyle = Style.SHAPE_PIN;
        gr.drawPolygon(p, arrowStyle);

        // arrow
        VectorFloat lot = new VectorFloat(difTo.getYFloat(), -difTo.getXFloat()).mul(0.5f);
        gr.drawPolygon(new Polygon(false)
                .add(end.add(difTo.add(lot).mul(0.2f)))
                .add(arrowTip)
                .add(end.add(difTo.sub(lot).mul(0.2f))), arrowStyle);
        if (condition != null && condition.length() > 0) {
            gr.drawText(getPos(), getPos().add(new Vector(1, 0)), condition, Orientation.CENTERCENTER, Style.NORMAL);
        }
    }

    /**
     * Initializes the position of the transition
     */
    public void initPos() {
        setPos(fromState.getPos().add(toState.getPos()).mul(0.5f)
                .add(new VectorFloat((float) Math.random() - 0.5f, (float) Math.random() - 0.5f).mul(30)));
    }

    /**
     * Sets the condition
     *
     * @param condition the condition
     */
    public void setCondition(String condition) {
        this.condition = condition;
        conditionExpression = null;
    }

    /**
     * @return returns the condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * @return the condition
     * @throws FiniteStateMachineException FiniteStateMachineException
     */
    public Expression getConditionExpression() throws FiniteStateMachineException {
        if (conditionExpression == null) {
            if (condition != null && condition.trim().length() > 0)
                try {
                    ArrayList<Expression> ex = new Parser(condition).parse();
                    if (ex.size() != 1)
                        throw new FiniteStateMachineException(Lang.get("err_fsmErrorInCondition_N", condition));

                    this.conditionExpression = ex.get(0);
                } catch (IOException | ParseException e) {
                    throw new FiniteStateMachineException(Lang.get("err_fsmErrorInCondition_N", condition), e);
                }
        }
        return conditionExpression;
    }

    /**
     * @return true if this transition has a condition
     * @throws FiniteStateMachineException FiniteStateMachineException
     */
    public boolean hasCondition() throws FiniteStateMachineException {
        return getConditionExpression() != null;
    }

    /**
     * @return the starting state
     */
    public State getStartState() {
        return fromState;
    }

    /**
     * @return the target state
     */
    public State getTargetState() {
        return toState;
    }

    /**
     * Gives true if the position matches the transition.
     *
     * @param pos the position
     * @return true if pos matches the transition
     */
    public boolean matches(Vector pos) {
        return pos.sub(getPos()).len() < 50;
    }

    @Override
    public String toString() {
        return fromState + " --[" + condition + "]-> " + toState;
    }

    /**
     * Mark this transition as initial transition
     */
    public void setInitial() {
        isInitial = true;
    }
}
