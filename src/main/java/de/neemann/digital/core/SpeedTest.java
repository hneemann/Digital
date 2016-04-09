package de.neemann.digital.core;

import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class SpeedTest {
    private static final int LOOPCOUNTER = 2000;
    private final Model model;

    public SpeedTest(Model model) {
        this.model = model;
    }

    public double calculate() throws NodeException {
        ArrayList<Clock> clocks = model.getClocks();
        if (clocks.isEmpty())
            throw new NodeException(Lang.get("err_noClockFound"));
        else if (clocks.size() > 1)
            throw new NodeException(Lang.get("err_moreThenOneClocksFound"));


        Clock clock = clocks.get(0);
        model.init();
        ObservableValue clockValue = clock.getOutputs()[0];
        int state = (int) clockValue.getValue();

        long aktTime;
        long starTime = System.currentTimeMillis();
        int loops = 0;
        do {
            for (int i = 0; i < LOOPCOUNTER; i++) {
                state = 1 - state;
                clockValue.setValue(state);
            }
            loops++;
            aktTime = System.currentTimeMillis();
        } while (aktTime - starTime < 1000);

        long cycles = ((long) loops) * LOOPCOUNTER / 2;
        double time = (aktTime - starTime) / 1000.0;

        double freqency = cycles / time;

        System.out.println("cycles: " + cycles);
        System.out.println("time  : " + time + "s");
        System.out.println("freq  :" + freqency);

        return freqency;
    }
}
