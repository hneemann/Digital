package de.neemann.digital.gui;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Wizard for pin numbering
 * Created by hneemann on 14.05.17.
 */
public class NumberingWizard extends JDialog implements CircuitComponent.WizardNotification {
    private final CircuitComponent circuitComponent;
    private final JLabel label;
    private int pinNumber;

    /**
     * Creates a new instance
     * @param parent the parent frame
     * @param circuitComponent the component used to select the inputs and outputs
     */
    public NumberingWizard(JFrame parent, CircuitComponent circuitComponent) {
        super(parent, Lang.get("msg_numberingWizard"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.circuitComponent = circuitComponent;
        pinNumber = 0;
        label = new JLabel("________________");
        getContentPane().add(label);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                circuitComponent.deactivateWizard();
            }
        });
        pack();
        incPinNumber();
        setAlwaysOnTop(true);
        setLocation(parent.getLocation());
    }

    private void incPinNumber() {
        pinNumber++;
        label.setText(Lang.get("msg_pin_N", pinNumber));
    }

    /**
     * Start the wizard
     */
    public void start() {
        setVisible(true);
        circuitComponent.activateWizard(this);
    }

    @Override
    public void notify(VisualElement clicked) {
        if (clicked.equalsDescription(In.DESCRIPTION) || clicked.equalsDescription(Out.DESCRIPTION)) {
            clicked.getElementAttributes().set(Keys.PINNUMBER, pinNumber);
            incPinNumber();
            circuitComponent.hasChanged();
        }
    }

    @Override
    public void closed() {
        dispose();
    }
}
