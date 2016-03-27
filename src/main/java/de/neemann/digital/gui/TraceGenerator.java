package de.neemann.digital.gui;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelDescription;
import de.neemann.digital.draw.model.ModelEntry;

/**
 * @author hneemann
 */
public class TraceGenerator implements ModelStateObserver {

    private final Model model;

    public TraceGenerator(ModelDescription modelDescription, Model model, ElementLibrary library) {
        this.model = model;
        for (ModelEntry me : modelDescription) {
            String name = me.getVisualElement().getElementName();
            if (library.getElementType(name) == In.DESCRIPTION)
                register(me);
            if (library.getElementType(name) == Out.DESCRIPTION)
                register(me);
            if (library.getElementType(name) == Out.LEDDESCRIPTION)
                register(me);
            if (library.getElementType(name) == Out.PROBEDESCRIPTION)
                register(me);
        }
        model.addObserver(this);
    }

    private void register(ModelEntry me) {
        ObservableValue value = me.getPins().get(0).getValue();
        String labelName = me.getVisualElement().getElementAttributes().get(AttributeKey.Label);
        if (value != null && labelName != null && labelName.length() > 0) {


        }
    }


    @Override
    public void handleEvent(ModelEvent event) {
        switch (event.getType()) {
            case STEP:
                break;
            case STOPPED:
                model.removeObserver(this);
                break;
        }
    }

}
