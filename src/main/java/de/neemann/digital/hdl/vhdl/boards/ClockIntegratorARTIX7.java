package de.neemann.digital.hdl.vhdl.boards;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.hdl.model.*;

/**
 * Implements clocking on Artix 7
 */
public class ClockIntegratorARTIX7 implements ClockIntegrator {
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
    public void integrateClocks(HDLModel model) throws HDLException {

        if (model.getClocks().size() > 1)
            throw new HDLException("up to now only a single clock is supported on ARTIX-7");


        Params p = new Parameters(model.getClocks().get(0).getFrequency(), clkInPeriod).getBest();
        if (p == null)
            new ClockIntegratorGeneric(clkInPeriod).integrateClocks(model);
        else {
            insertMMCMClock(model, p);
        }
    }

    private void insertMMCMClock(HDLModel model, Params p) throws HDLException {
        Port cOut = new Port("out", Port.Direction.out).setBits(1);
        Port cIn = new Port("in", Port.Direction.in).setBits(1);

        Signal oldSig = model.getClocks().get(0).getClockPort().getSignal();
        Signal newSig = model.createSignal();
        oldSig.replaceWith(newSig);
        newSig.addPort(cOut);
        oldSig.addPort(cIn);

        ElementAttributes attr = new ElementAttributes()
                .set(new Key<>("D_PARAM", 0), p.d)
                .set(new Key<>("M_PARAM", 0), p.m)
                .set(new Key<>("DIV_PARAM", 0), p.divider)
                .set(new Key<>("PERIOD_PARAM", 0.0), clkInPeriod);

        model.addNode(new HDLNode(new Ports().add(cIn).add(cOut), "MMCME2_BASE", attr));
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
                    int fVco = (int) (fInMHz * m / d);
                    int fpdf1 = (int) (fInMHz / d);
                    int fpdf2 = fVco / m;

                    boolean valid = (F_VCO_MIN_MHZ <= fVco) && (fVco <= F_VCO_MAX_MHZ)
                            && (F_PFD_MIN_MHZ <= fpdf1) && (fpdf1 <= F_PFD_MAX_MHZ)
                            && (F_PFD_MIN_MHZ <= fpdf2) && (fpdf2 <= F_PFD_MAX_MHZ);

                    if (valid) {
                        int divider = (int) (fVco / targetFreqInMHz);
                        if (divider >= 1 && divider <= MAX_CLOCK_DIVIDE) {

                            double f = fInMHz * m / (d * divider);

                            double error = (F_VCO_MAX_MHZ - fVco) + Math.abs(f - targetFreqInMHz) * 10 + (Math.abs(m - mIdeal) * 10);

                            if (best == null || best.error > error)
                                best = new Params(m, d, divider, f, error);

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

        private Params(int m, int d, int divider, double f, double error) {
            this.m = m;
            this.d = d;
            this.divider = divider;
            this.f = f;
            this.error = error;
        }

        @Override
        public String toString() {
            return "Params{"
                    + "m=" + m
                    + ", d=" + d
                    + ", divider=" + divider
                    + ", error=" + error
                    + ", f=" + f
                    + '}';
        }

        public int getD() {
            return d;
        }

        public int getM() {
            return m;
        }

        public int getDivider() {
            return divider;
        }
    }

}
