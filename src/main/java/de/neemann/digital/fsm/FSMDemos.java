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
     * Creates a debounced rotary switch decoder
     *
     * @return the fsm
     */
    public static FSM createRotDecoder() {
        State top = new State("top");
        State topSetLeft = new State("topSetLeft").val("L", 1);
        State topSetRight = new State("topSetRight").val("R", 1);
        State leftA = new State("leftA");
        State leftB = new State("leftB");
        State bottom = new State("bottom");
        State bottomSetLeft = new State("bottomSetRight").val("R", 1);
        State bottomSetRight = new State("bottomSetLeft").val("L", 1);
        State rightA = new State("rightA");
        State rightB = new State("rightB");
        return new FSM(top, topSetLeft, leftA, leftB, bottomSetLeft, bottom, bottomSetRight, rightB, rightA, topSetRight)
                .transition(top, leftA, "A & !B")
                .transition(top, rightA, "!A & B")
                .transition(topSetLeft, top, null)
                .transition(topSetRight, top, null)

                .transition(rightA, top, "!A & !B")
                .transition(rightB, topSetRight, "!A & !B")
                .transition(leftA, top, "!A & !B")
                .transition(leftB, topSetLeft, "!A & !B")

                .transition(bottom, leftB, "A & !B")
                .transition(bottom, rightB, "!A & B")
                .transition(bottomSetLeft, bottom, null)
                .transition(bottomSetRight, bottom, null)

                .transition(rightB, bottom, "A & B")
                .transition(rightA, bottomSetRight, "A & B")
                .transition(leftB, bottom, "A & B")
                .transition(leftA, bottomSetLeft, "A & B");
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
            State s = new State(Integer.toString(i)).setNumber(i);
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
        State red = new State("red").setNumber(0).val("R", 1);
        State redYellow = new State("red/yellow").setNumber(1).val("R", 1).val("Y", 1);
        State green = new State("green").setNumber(2).val("G", 1);
        State yellow = new State("yellow").setNumber(3).val("Y", 1);
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
        State red = new State("red").setNumber(1).val("R", 1);
        State redYellow = new State("red/yellow").setNumber(3).val("R", 1).val("Y", 1);
        State green = new State("green").setNumber(4).val("G", 1);
        State yellow = new State("yellow").setNumber(2).val("Y", 1);
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
        State s0 = new State("s0").setNumber(0).val("P0", 1).val("P1", 1);
        State s1 = new State("s1").setNumber(1).val("P1", 1).val("P2", 1);
        State s2 = new State("s2").setNumber(2).val("P2", 1).val("P3", 1);
        State s3 = new State("s3").setNumber(3).val("P3", 1).val("P0", 1);
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
        State init = new State("init").setNumber(0);
        State s0 = new State("s0").setNumber(3).val("P0", 1).val("P1", 1);
        State s1 = new State("s1").setNumber(6).val("P1", 1).val("P2", 1);
        State s2 = new State("s2").setNumber(12).val("P2", 1).val("P3", 1);
        State s3 = new State("s3").setNumber(9).val("P3", 1).val("P0", 1);
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
