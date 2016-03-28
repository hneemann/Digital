package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;

import javax.swing.*;

/**
 * @author hneemann
 */
public class Clock implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription("Clock", Clock.class)
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Frequency);

    private final ObservableValue output;
    private final int frequency;
    private MyModelStateObserver observer;

    public Clock(ElementAttributes attributes) {
        output = new ObservableValue("C", 1);
        int f = attributes.get(AttributeKey.Frequency);
        if (f < 1) f = 1;
        frequency = f;
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        throw new NodeException(Lang.get("err_noInputsAvailable"), null);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    public void disableTimer() {
        observer.remove();
    }

    @Override
    public void registerNodes(Model model) {
        observer = new MyModelStateObserver(model, this, frequency, output);
        model.addObserver(observer);
    }

    private static class MyModelStateObserver implements ModelStateObserver {
        private final Model model;
        private final Clock clock;
        private final int frequency;
        private final ObservableValue output;
        private Timer timer;

        public MyModelStateObserver(Model model, Clock clock, int frequency, ObservableValue output) {
            this.model = model;
            this.clock = clock;
            this.frequency = frequency;
            this.output = output;
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
                case FETCHCLOCK:
                    event.registerClock(clock);
                    break;
            }
        }

        public void remove() {
            model.removeObserver(this);
        }
    }
}
