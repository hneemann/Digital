/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.boards;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.hdl.model2.*;
import de.neemann.digital.hdl.model2.clock.ClockInfo;
import de.neemann.digital.hdl.model2.clock.ClockIntegratorGeneric;
import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;

import java.util.ArrayList;

/**
 * Implements clocking on Artix 7
 */
public class ClockIntegratorARTIX7 implements HDLClockIntegrator {
    private static final int F_PFD_MIN_MHZ = 10;
    private static final int F_PFD_MAX_MHZ = 450;

    private static final int F_VCO_MIN_MHZ = 600;
    private static final int F_VCO_MAX_MHZ = 1200;

    private static final int MAX_CLOCK_DIVIDE = 128;

    private final double clkInPeriod;

    /**
     * Creates a new instance
     *
     * @param clkInPeriod clock in period in ns
     */
    public ClockIntegratorARTIX7(double clkInPeriod) {
        this.clkInPeriod = clkInPeriod;
    }

    @Override
    public void integrateClocks(HDLCircuit model, ArrayList<ClockInfo> clocks) throws HDLException {
        if (clocks.size() > 1)
            throw new HDLException("up to now only a single clock is supported on ARTIX-7");


        Params p = new Parameters(clocks.get(0).getFrequency(), clkInPeriod).getBest();
        if (p == null)
            new ClockIntegratorGeneric(clkInPeriod).integrateClocks(model, clocks);
        else
            insertMMCMClock(model, p, clocks.get(0).getClockPort());
    }

    private void insertMMCMClock(HDLCircuit model, Params p, HDLPort clock) throws HDLException {
        ElementAttributes attr = new ElementAttributes()
                .set(new Key<>("cascading", 0), p.isCascading())
                .set(new Key<>("D_PARAM", 0), p.d)
                .set(new Key<>("M_PARAM", 0), p.m)
                .set(new Key<>("DIV_PARAM", 0), p.divider)
                .set(new Key<>("DIV4_PARAM", 0), p.divider4)
                .set(new Key<>("PERIOD_PARAM", 0.0), clkInPeriod);

        model.integrateClockNode(clock, new HDLNodeBuildIn("MMCME2_BASE", attr, name -> 1));
    }

    static final class Parameters {

        private final Params best;

        Parameters(int frequency, double clkInPeriod) {
            double fInMHz = 1000 / clkInPeriod;
            double targetFreqInMHz = ((double) frequency) / 1000 / 1000;

            int dMin = (int) Math.ceil(fInMHz / F_PFD_MAX_MHZ);
            int dMax = (int) Math.floor(fInMHz / F_PFD_MIN_MHZ);
            int mMin = (int) Math.ceil(F_VCO_MIN_MHZ / fInMHz * dMin);
            int mMax = (int) Math.floor(F_VCO_MIN_MHZ / fInMHz * dMax);

            int mIdeal = (int) Math.floor(dMin * F_VCO_MAX_MHZ / fInMHz);

            Params best = null;

            for (int m = mMin; m <= mMax; m++)
                for (int d = dMin; d <= dMax; d++) {
                    double fVco = fInMHz * m / d;
                    double fpdf = fVco / m;

                    boolean valid = (F_VCO_MIN_MHZ <= fVco) && (fVco <= F_VCO_MAX_MHZ)
                            && (F_PFD_MIN_MHZ <= fpdf) && (fpdf <= F_PFD_MAX_MHZ);

                    if (valid) {
                        int divider = (int) (fVco / targetFreqInMHz);
                        if (divider >= 1 && divider <= MAX_CLOCK_DIVIDE) {
                            double f = fVco / divider;

                            double error = (F_VCO_MAX_MHZ - fVco) + Math.abs(f - targetFreqInMHz) * 10 + (Math.abs(m - mIdeal) * 10);

                            if (best == null || best.error > error)
                                best = new Params(m, d, divider, f, error);

                        } else {
                            if (divider > MAX_CLOCK_DIVIDE && divider <= MAX_CLOCK_DIVIDE * MAX_CLOCK_DIVIDE) {
                                int divider4 = 0;
                                int divider6 = 0;

                                int bestErr = Integer.MAX_VALUE;
                                for (int d6 = 1; d6 <= MAX_CLOCK_DIVIDE; d6++)
                                    for (int d4 = 1; d4 <= MAX_CLOCK_DIVIDE; d4++) {
                                        int dd = d4 * d6;
                                        int err = Math.abs(divider - dd);
                                        if (err < bestErr) {
                                            bestErr = err;
                                            divider4 = d4;
                                            divider6 = d6;
                                        }
                                    }


                                if (divider4 > 0 && divider6 > 0) {
                                    double f = fVco / divider6 / divider4;

                                    double error = (F_VCO_MAX_MHZ - fVco) + Math.abs(f - targetFreqInMHz) * 10 + (Math.abs(m - mIdeal) * 10);

                                    if (best == null || best.error > error)
                                        best = new Params(m, d, divider6, divider4, f, error);
                                }
                            }
                        }
                    }
                }
            this.best = best;
        }

        Params getBest() {
            return best;
        }
    }

    static final class Params {
        private final int m;
        private final int d;
        private final int divider;
        private final double error;
        private final double f;
        private final int divider4;

        private Params(int m, int d, int divider, double f, double error) {
            this.m = m;
            this.d = d;
            this.divider = divider;
            this.divider4 = 0;
            this.f = f;
            this.error = error;
        }

        private Params(int m, int d, int divider, int divider4, double f, double error) {
            this.m = m;
            this.d = d;
            this.divider = divider;
            this.divider4 = divider4;
            this.f = f;
            this.error = error;
        }


        @Override
        public String toString() {
            return "Params{"
                    + "m=" + m
                    + ", d=" + d
                    + ", divider=" + divider
                    + ", div4=" + divider4
                    + ", error=" + error
                    + ", f=" + f
                    + '}';
        }

        private boolean isCascading() {
            return divider4 != 0;
        }

    }

}
