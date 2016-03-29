package de.neemann.digital.draw.model;

import de.neemann.digital.core.*;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.gui.ErrorMessage;

import javax.swing.*;

/**
 * @author hneemann
 */
public class RealTimeClock implements ModelStateObserver {

    private final Model model;
    private final int frequency;
    private final ObservableValue output;
    private Timer timer;

    public RealTimeClock(Model model, Clock clock) {
        this.model = model;
        int f = clock.getFrequency();
        if (f < 1) f = 1;
        this.frequency = f;
        this.output = clock.getClockOutput();
    }

    @Override
    public void handleEvent(ModelEvent event) {
        switch (event.getType()) {
            case STARTED:
                int delay = 1000 / frequency;
                if (delay < 100) delay = 100;
                timer = new Timer(delay, e -> {
                    output.setValue(1 - output.getValue());
                    try {
                        model.doStep();
                    } catch (NodeException e1) {
                        SwingUtilities.invokeLater(new ErrorMessage("ClockError").addCause(e1));
                        timer.stop();
                    }
                });
                timer.start();
                break;
            case STOPPED:
                if (timer != null)
                    timer.stop();
                break;
        }
    }
}
