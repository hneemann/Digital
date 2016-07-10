package de.neemann.digital.gui.components.terminal;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

import javax.swing.*;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * @author hneemann
 */
public class Keyboard extends Node implements Element {

    /**
     * The keyboard description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Keyboard.class,
            input("C"),
            input("sel", Lang.get("elem_Keyboard_pin_sel")))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .setShortName(Lang.get("elem_Keyboard"));

    private static final Object KEYBOARD_LOCK = new Object();
    private static KeyboardDialog keyboardDialog;

    private ObservableValue data;
    private ObservableValue clock;
    private ObservableValue select;
    private boolean lastClock;
    private int keyData;
    private boolean sel;

    /**
     * Creates a new terminal instance
     *
     * @param attributes the attributes
     */
    public Keyboard(ElementAttributes attributes) {
        data = new ObservableValue("D", 16).setDescription(Lang.get("elem_Keyboard_pin_D"));
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        clock = inputs.get(0).addObserverToValue(this).checkBits(1, this);
        select = inputs.get(1).addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return data.asList();
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clockVal = clock.getBool();
        sel = select.getBool();
        if (!lastClock && clockVal) {
            KeyboardDialog kbd;
            synchronized (KEYBOARD_LOCK) {
                kbd = keyboardDialog;
            }
            if (kbd==null)
                keyData=0;
            else
                keyData = keyboardDialog.getChar();
        }
        lastClock = clockVal;
    }

    @Override
    public void writeOutputs() throws NodeException {
        data.set(keyData, !sel);
    }

    @Override
    public void init(Model model) throws NodeException {
        SwingUtilities.invokeLater(() -> {
            if (keyboardDialog == null || !keyboardDialog.isVisible()) {
                synchronized (KEYBOARD_LOCK) {
                    keyboardDialog = new KeyboardDialog(null);
                }
                keyboardDialog.setVisible(true);
            }
        });
        model.addObserver(event -> {
            if (event.equals(ModelEvent.STOPPED)) {
                if (keyboardDialog != null) {
                    KeyboardDialog kd = keyboardDialog;
                    SwingUtilities.invokeLater(kd::dispose);
                    synchronized (KEYBOARD_LOCK) {
                        keyboardDialog = null;
                    }
                }
            }
        });
    }
}
