/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

/**
 * Provides some demo fsm's
 */
public final class FSMDemos {

    private FSMDemos() {
    }


    /**
     * Blink
     *
     * @return the fsm
     */
    public static FSM blink() {
        State off = new State("off");
        State on = new State("on");
        return new FSM(off, on)
                .transition(on, off, null)
                .transition(off, on, null);
    }

    /**
     * Creates a debounced rotary switch decoder
     *
     * @return the fsm
     */
    public static FSM rotDecoder() {
        State init = new State("init");
        State top = new State("top");
        State topSetLeft = new State("topSetLeft").setValues("L=1");
        State topSetRight = new State("topSetRight").setValues("R=1");
        State leftTop = new State("leftTop");
        State leftBottom = new State("leftBottom");
        State bottom = new State("bottom");
        State bottomSetLeft = new State("bottomSetRight").setValues("R=1");
        State bottomSetRight = new State("bottomSetLeft").setValues("L=1");
        State rightTop = new State("rightTop");
        State rightBottom = new State("rightBottom");
        return new FSM(init, top, topSetLeft, leftTop, leftBottom, bottomSetLeft, bottom, bottomSetRight, rightBottom, rightTop, topSetRight)
                .transition(init, top, "A=0 & B=0")
                .transition(init, bottom, "A=1 & B=1")

                .transition(top, leftTop, "A=1 & B=0")
                .transition(top, rightTop, "A=0 & B=1")
                .transition(topSetLeft, top, null)
                .transition(topSetRight, top, null)

                .transition(rightTop, top, "A=0 & B=0")
                .transition(rightBottom, topSetRight, "A=0 & B=0")
                .transition(leftTop, top, "A=0 & B=0")
                .transition(leftBottom, topSetLeft, "A=0 & B=0")

                .transition(bottom, leftBottom, "A=1 & B=0")
                .transition(bottom, rightBottom, "A=0 & B=1")
                .transition(bottomSetLeft, bottom, null)
                .transition(bottomSetRight, bottom, null)

                .transition(rightBottom, bottom, "A=1 & B=1")
                .transition(rightTop, bottomSetRight, "A=1 & B=1")
                .transition(leftBottom, bottom, "A=1 & B=1")
                .transition(leftTop, bottomSetLeft, "A=1 & B=1");
    }

    /**
     * Creates a counter
     *
     * @param n the number of states
     * @return the fsm
     */
    public static FSM counter(int n) {
        FSM fsm = new FSM();
        State last = null;
        for (int i = 0; i < n; i++) {
            State s = new State("").setNumber(i);
            fsm.add(s);
            if (last != null)
                fsm.transition(last, s, null);
            last = s;
        }
        fsm.transition(last, fsm.getStates().get(0), null);

        return fsm;
    }

    /**
     * Creates a traffic light fsm
     *
     * @return the fsm
     */
    public static FSM trafficLight() {
        State red = new State("red").setNumber(0).setValues("R=1");
        State redYellow = new State("red/yellow").setNumber(1).setValues("R=1,Y=1");
        State green = new State("green").setNumber(2).setValues("G=1");
        State yellow = new State("yellow").setNumber(3).setValues("Y=1");
        return new FSM(red, redYellow, green, yellow)
                .transition(red, redYellow, "!Stop")
                .transition(redYellow, green, null)
                .transition(green, yellow, null)
                .transition(yellow, red, null);
    }

    /**
     * Creates a traffic light fsm
     *
     * @return the fsm
     */
    public static FSM trafficLightMedwedew() {
        State init = new State("init").setNumber(0);
        State red = new State("red").setNumber(1).setValues("R=1");
        State redYellow = new State("red/yellow").setNumber(3).setValues("R=1, Y=1");
        State green = new State("green").setNumber(4).setValues("G=1");
        State yellow = new State("yellow").setNumber(2).setValues("Y=1");
        return new FSM(init, red, redYellow, green, yellow)
                .transition(init, red, null)
                .transition(red, redYellow, "!Stop")
                .transition(redYellow, green, null)
                .transition(green, yellow, null)
                .transition(yellow, red, null);
    }

    /**
     * Creates a traffic light fsm
     *
     * @return the fsm
     */
    public static FSM selCounter() {
        State s0 = new State("s0").setNumber(0);
        State s1 = new State("s1").setNumber(1);
        State s2 = new State("s2").setNumber(2);
        State s3 = new State("s3").setNumber(3);
        return new FSM(s0, s1, s2, s3)
                .transition(s0, s1, null)
                .transition(s0, s0, "!T0  !T1")
                .transition(s1, s2, null)
                .transition(s1, s0, "T0 !T1")
                .transition(s2, s3, null)
                .transition(s2, s0, "!T0 T1")
                .transition(s3, s0, null);
    }

    /**
     * Creates a stepper controller
     *
     * @return the fsm
     */
    public static FSM stepper() {
        State s0 = new State("s0").setNumber(0).setValues("P0=1,P1=1");
        State s1 = new State("s1").setNumber(1).setValues("P1=1,P2=1");
        State s2 = new State("s2").setNumber(2).setValues("P2=1,P3=1");
        State s3 = new State("s3").setNumber(3).setValues("P3=1,P0=1");
        return new FSM(s0, s1, s2, s3)
                .transition(s0, s1, "D")
                .transition(s1, s2, "D")
                .transition(s2, s3, "D")
                .transition(s3, s0, "D")
                .transition(s1, s0, "!D")
                .transition(s2, s1, "!D")
                .transition(s3, s2, "!D")
                .transition(s0, s3, "!D");
    }

    /**
     * Creates a stepper controller
     *
     * @return the fsm
     */
    public static FSM stepperMedwedew() {
        State init = new State("init").setNumber(0).setValues("P0=2,P1=2,P2=2,P3=2");
        State s0 = new State("s0").setNumber(3).setValues("P0=1,P1=1");
        State s1 = new State("s1").setNumber(6).setValues("P1=1,P2=1");
        State s2 = new State("s2").setNumber(12).setValues("P2=1,P3=1");
        State s3 = new State("s3").setNumber(9).setValues("P3=1,P0=1");
        return new FSM(init, s0, s1, s2, s3)
                .transition(init, s0, null)
                .transition(s0, s1, "D")
                .transition(s1, s2, "D")
                .transition(s2, s3, "D")
                .transition(s3, s0, "D")
                .transition(s1, s0, "!D")
                .transition(s2, s1, "!D")
                .transition(s3, s2, "!D")
                .transition(s0, s3, "!D");
    }
}
