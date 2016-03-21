package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.process.utils.gui.ErrorMessage;

import javax.swing.*;

/**
 * @author hneemann
 */
public class Clock implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription("Cl", Clock.class)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Frequency);

    private final ObservableValue output;
    private final int frequency;
    public boolean startThisTimer = true;

    public Clock(ElementAttributes attributes) {
        output = new ObservableValue("C", 1);
        frequency = attributes.get(AttributeKey.Frequency);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        throw new NodeException("no inputs available!");
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    public void disableTimer() {
        this.startThisTimer = false;
    }

    @Override
    public void registerNodes(Model model) {
        model.addObserver(new ModelStateObserver() {
            public Timer timer;

            @Override
            public void handleEvent(ModelEvent event) {
                switch (event.getType()) {
                    case STARTED:
                        if (startThisTimer) {
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
                        }
                        break;
                    case STOPPED:
                        if (timer != null)
                            timer.stop();
                        break;
                    case FETCHCLOCK:
                        event.registerClock(Clock.this);
                        break;
                }
            }
        });
    }
}
