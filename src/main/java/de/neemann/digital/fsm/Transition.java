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
public class Transition extends Movable<Transition> {
    private static final float EXPANSION_TRANS = 0.001f;

    private final State fromState;
    private final State toState;
    private String condition;
    private transient Expression conditionExpression;


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
        this.condition = condition == null ? "" : condition;
        initPos();
    }

    /**
     * Calculates the repulsive forces
     *
     * @param states      the states
     * @param transitions the transitions
     */
    void calcForce(List<State> states, List<Transition> transitions) {
        float preferredDist = Math.max(fromState.getVisualRadius(), toState.getVisualRadius()) * 5;
        calcForce(preferredDist, states, transitions);
    }

    /**
     * Calculates the repulsive forces
     *
     * @param preferredDist the preferred distance
     * @param states        the states
     * @param transitions   the transitions
     */
    void calcForce(float preferredDist, List<State> states, List<Transition> transitions) {

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

        if (!isInitialTransition()) {
            for (State s : states)
                addRepulsive(s.getPos(), 2000);

            for (Transition t : transitions)
                if (t != this)
                    addRepulsive(t.getPos(), 800);
        }
    }

    private boolean isInitialTransition() {
        return getFsm() != null && getFsm().isInitial(this);
    }

    @Override
    public void setPos(VectorFloat position) {
        if (fromState != toState) {
            VectorFloat dist = fromState.getPos().sub(toState.getPos());
            if (dist.getXFloat() != 0 || dist.getYFloat() != 0) {
                VectorFloat p = position.sub(fromState.getPos());
                VectorFloat n = dist.getOrthogonal().norm();
                float l = p.mul(n);
                super.setPos(fromState.getPos().sub(dist.mul(0.5f)).add(n.mul(l)));
                return;
            }
        }
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

        VectorFloat difFrom = anchorFrom.sub(fromState.getPos()).norm().mul(fromState.getVisualRadius() + Style.MAXLINETHICK);
        VectorFloat difTo = anchorTo.sub(toState.getPos()).norm().mul(toState.getVisualRadius() + Style.MAXLINETHICK + 2);
        VectorFloat difToTip = anchorTo.sub(toState.getPos()).norm().mul(toState.getVisualRadius() + Style.MAXLINETHICK);

        final VectorFloat start = fromState.getPos().add(difFrom);
        final VectorFloat end = toState.getPos().add(difTo);
        final VectorFloat arrowTip = toState.getPos().add(difToTip);

        Polygon p = new Polygon(false)
                .add(start)
                .add(anchorFrom, anchorTo, end);
        final Style arrowStyle = Style.SHAPE_PIN;
        gr.drawPolygon(p, arrowStyle);

        // arrow
        VectorFloat lot = difTo.getOrthogonal().mul(0.5f);
        gr.drawPolygon(new Polygon(false)
                .add(end.add(difTo.add(lot).mul(0.2f)))
                .add(arrowTip)
                .add(end.add(difTo.sub(lot).mul(0.2f))), arrowStyle);
        if (condition != null && condition.length() > 0) {
            gr.drawText(getPos(), getPos().add(new Vector(1, 0)), condition, Orientation.CENTERCENTER, Style.INOUT);
        }
        if (getValues() != null && getValues().length() > 0) {
            VectorFloat pos = getPos().add(new VectorFloat(0, Style.NORMAL.getFontSize()));
            gr.drawText(pos, pos.add(new Vector(1, 0)), Lang.get("fsm_set_N", getValues()), Orientation.CENTERCENTER, Style.INOUT);
        }
    }

    /**
     * Initializes the position of the transition
     */
    void initPos() {
        setPos(fromState.getPos().add(toState.getPos()).mul(0.5f)
                .add(new VectorFloat((float) Math.random() - 0.5f, (float) Math.random() - 0.5f).mul(2)));
    }

    /**
     * Sets the condition
     *
     * @param condition the condition
     */
    public void setCondition(String condition) {
        if (!this.condition.equals(condition)) {
            this.condition = condition;
            wasModified();
            conditionExpression = null;
            if (getFsm() != null)
                getFsm().resetInitInitialization();
        }
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
    Expression getConditionExpression() throws FiniteStateMachineException {
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
    boolean hasCondition() throws FiniteStateMachineException {
        return getConditionExpression() != null;
    }

    /**
     * @return the starting state
     */
    State getStartState() {
        return fromState;
    }

    /**
     * @return the target state
     */
    State getTargetState() {
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

}
