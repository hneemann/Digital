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

    Transition(Transition other, ArrayList<State> states, List<State> otherStates) {
        this.condition = other.condition;
        this.fromState = states.get(otherStates.indexOf(other.fromState));
        this.toState = states.get(otherStates.indexOf(other.toState));
        setValues(other.getValues());
        setPos(other.getPos());
    }

    /**
     * Calculates the repulsive forces
     *
     * @param states      the states
     * @param transitions the transitions
     */
    void calcForce(List<State> states, List<Transition> transitions) {
        float preferredDist = Math.max(fromState.getVisualRadius(), toState.getVisualRadius());
        calcForce(preferredDist * 5, states, transitions);
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

        for (State s : states)
            if ((s == fromState) == (s == toState))
                addRepulsive(s.getPos(), 2000);

        for (Transition t : transitions)
            if (t != this)
                addRepulsive(t.getPos(), 1500);
    }


    @Override
    public void setPosDragging(VectorFloat position) {
        super.setPosDragging(posConstrain(position));
    }

    @Override
    public void setPos(VectorFloat position) {
        super.setPos(posConstrain(position));
    }

    private VectorFloat posConstrain(VectorFloat position) {
        if (fromState != toState) {
            VectorFloat dist = toState.getPos().sub(fromState.getPos());
            if (dist.getXFloat() != 0 || dist.getYFloat() != 0) {
                dist = dist.norm();
                VectorFloat start = fromState.getPos().add(dist.mul(fromState.getVisualRadius()));
                VectorFloat end = toState.getPos().sub(dist.mul(toState.getVisualRadius()));

                VectorFloat p = position.sub(start);
                VectorFloat n = dist.getOrthogonal();
                float l = p.mul(n);
                return start.add(end).div(2).add(n.mul(l));
            }
        }
        return position;
    }

    /**
     * Draws the transition
     *
     * @param gr the Graphic instance to draw to
     */
    public void drawTo(Graphic gr) {
        Style style = Style.SHAPE_PIN;
        if (getFsm() != null)
            if (getFsm().getActiveTransition() == this)
                style = Style.HIGHLIGHT;


        final VectorFloat start;
        final VectorFloat anchor;
        final VectorFloat end;
        final VectorFloat anchor0;
        if (fromState == toState) {
            VectorFloat dif = getPos().sub(fromState.getPos()).getOrthogonal().mul(0.5f);
            VectorFloat ps = getPos().add(dif);
            VectorFloat pe = getPos().sub(dif);
            start = fromState.getPos().add(
                    ps.sub(fromState.getPos()).norm().mul(fromState.getVisualRadius() + Style.MAXLINETHICK));
            end = toState.getPos().add(
                    pe.sub(toState.getPos()).norm().mul(toState.getVisualRadius() + Style.MAXLINETHICK + 2));

            VectorFloat d0 = start.sub(fromState.getPos());
            VectorFloat d3 = end.sub(toState.getPos());
            float t;
            if (Math.abs(d0.getXFloat() + d3.getXFloat()) > Math.abs(d0.getYFloat() + d3.getYFloat()))
                t = -4 * (start.getXFloat() + end.getXFloat() - 2 * getPos().getXFloat()) / (3 * (d0.getXFloat() + d3.getXFloat()));
            else
                t = -4 * (start.getYFloat() + end.getYFloat() - 2 * getPos().getYFloat()) / (3 * (d0.getYFloat() + d3.getYFloat()));
            anchor0 = start.add(d0.mul(t));
            anchor = end.add(d3.mul(t));
        } else {
            float n = 1.75f;
            VectorFloat correctedPos = getPos().mul(n).add(fromState.getPos().add(toState.getPos()).mul((1 - n) / 2));
            start = fromState.getPos().add(
                    correctedPos.sub(fromState.getPos()).norm().mul(fromState.getVisualRadius() + Style.MAXLINETHICK));
            end = toState.getPos().add(
                    correctedPos.sub(toState.getPos()).norm().mul(toState.getVisualRadius() + Style.MAXLINETHICK + 2));
            anchor = getPos().mul(2).sub(start.div(2)).sub(end.div(2));
            anchor0 = null;
        }

        drawArrow(gr, start, anchor0, anchor, end, style);

        // text
        ArrayList<String> strings = new ArrayList<>();
        if (condition != null && !condition.isEmpty())
            strings.add("$" + condition + "$");
        if (getValues() != null && !getValues().isEmpty())
            strings.add(Lang.get("fsm_set_N", "$" + getValues() + "$"));

        if (!strings.isEmpty()) {
            final int fontSize = Style.NORMAL.getFontSize();
            VectorFloat textPos = getPos().add(0, -fontSize * (strings.size() - 1) / 2f);

            if (fromState.getPos().getYFloat() < getPos().getYFloat()
                    && toState.getPos().getYFloat() < getPos().getYFloat()) {
                textPos = textPos.add(new VectorFloat(0, fontSize * strings.size() / 2f));
            }

            if (fromState.getPos().getYFloat() > getPos().getYFloat()
                    && toState.getPos().getYFloat() > getPos().getYFloat()) {
                textPos = textPos.add(new VectorFloat(0, -fontSize * strings.size() / 2f));
            }

            for (String s : strings) {
                gr.drawText(textPos, s, Orientation.CENTERCENTER, Style.NORMAL);
                textPos = textPos.add(0, fontSize);
            }
        }
    }

    static void drawArrow(Graphic gr, VectorInterface start, VectorInterface anchor0, VectorInterface anchor, VectorInterface end, Style arrowStyle) {
        if (anchor == null)
            anchor = start.add(end).div(2);

        // arrow line
        if (anchor0 != null)
            gr.drawPolygon(new Polygon(false).add(start).add(anchor0, anchor, end), arrowStyle);
        else
            gr.drawPolygon(new Polygon(false).add(start).add(anchor, end), arrowStyle);

        // arrowhead
        VectorFloat dir = anchor.sub(end).norm().mul(20);
        VectorFloat lot = dir.getOrthogonal().mul(0.3f);
        gr.drawPolygon(new Polygon(false)
                .add(end.add(dir).add(lot))
                .add(end.sub(dir.mul(0.1f)))
                .add(end.add(dir).sub(lot)), arrowStyle);
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
            wasModified(Property.CONDITION);
            conditionExpression = null;
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
