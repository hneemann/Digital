/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.boards;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLException;
import de.neemann.digital.hdl.model2.HDLNodeBuildIn;
import de.neemann.digital.hdl.model2.HDLPort;
import de.neemann.digital.hdl.model2.clock.ClockInfo;
import de.neemann.digital.hdl.model2.clock.ClockIntegratorGeneric;
import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;
import java.util.ArrayList;

/**
 *
 * @author ideras
 */
public class ClockIntegratorSpartan6 implements HDLClockIntegrator {
    private static final int MIN_FREQ = 5; // Minimum frequency in MHz
    private static final int MAX_FREQ = 333; // Maximum frequency in MHz
    private final BoardClockInfo[] boardClkInfo;

    /**
     * Initialize a new instance
     *
     * @param boardClkInfo the board clock pins information
     */
    public ClockIntegratorSpartan6(BoardClockInfo[] boardClkInfo) {
        this.boardClkInfo = boardClkInfo;
    }

    @Override
    public void integrateClocks(HDLCircuit circuit, ArrayList<ClockInfo> clocks) throws HDLException {
        if (clocks.size() > 1)
            throw new HDLException("up to now only a single clock is supported on Spartan-6");

        BoardClockInfo boardClockInfo = null;
        ClockInfo clkInfo = clocks.get(0);

        for (BoardClockInfo bci : boardClkInfo) {
            if (bci.getPinNumber().equals(clkInfo.getClockPort().getPinNumber())) {
                boardClockInfo = bci;
                break;
            }
        }
        if (boardClockInfo == null) {
            throw new HDLException("Cannot find clock pin '" + clkInfo.getClockPort().getPinNumber() + "'");
        }

        int freq = clkInfo.getFrequency();
        Params p = getParameters(freq, boardClockInfo.getClkPeriod());

        if (p == null) {
            new ClockIntegratorGeneric(boardClockInfo.getClkPeriod()).integrateClocks(circuit, clocks);
        } else {
            insertDCM(circuit, p, clkInfo.getClockPort(), boardClockInfo.getClkPeriod());
        }
    }

    private void insertDCM(HDLCircuit circuit, Params p, HDLPort clock, double clkInPeriod) throws HDLException {
        ElementAttributes attr = new ElementAttributes()
                .set(new Key<>("CLKFX_DIVIDE", 0), p.d)
                .set(new Key<>("CLKFX_MULTIPLY", 0), p.m)
                .set(new Key<>("CLKIN_PERIOD", 0.0), clkInPeriod);

        circuit.integrateClockNode(clock, new HDLNodeBuildIn("DCM_SP", attr, name -> 1));
    }

    Params getParameters(int targetFreq, double clkInPeriod) {
        double fInMHz = 1000 / clkInPeriod;
        double targetFreqInMHz = ((double) targetFreq) / 1000 / 1000;
        Params p = null;

        if (!(targetFreqInMHz >= MIN_FREQ && targetFreqInMHz <= MAX_FREQ)) {
            return null;
        }

        for (int m=2; m<32; m++) {
            for (int d=1; d<=32; d++) {
                double fGen = fInMHz * (double) m / (double) d;
                double error = Math.abs(fGen - targetFreqInMHz);

                if (p == null || p.error > error) {
                    p = new Params(m, d, fGen, error);
                }
            }
        }

        if (p.error < 0.01) {
            return p;
        } else {
            return null;
        }
    }

    static final class Params {
        private final int m;
        private final int d;
        private final double error;
        private final double f;

        private Params(int m, int d, double f, double error) {
            this.m = m;
            this.d = d;
            this.f = f;
            this.error = error;
        }

        @Override
        public String toString() {
            return "Params{"
                    + "m=" + m
                    + ", d=" + d
                    + ", error=" + error
                    + ", f=" + f
                    + '}';
        }

    }
}
