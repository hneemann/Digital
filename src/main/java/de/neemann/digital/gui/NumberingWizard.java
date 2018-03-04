/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.modification.ModifyAttribute;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.Screen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Wizard for pin numbering
 */
public class NumberingWizard extends JDialog implements CircuitComponent.WizardNotification {
    private final CircuitComponent circuitComponent;
    private final JLabel label;
    private int pinNumber;

    /**
     * Creates a new instance
     *
     * @param parent           the parent frame
     * @param circuitComponent the component used to select the inputs and outputs
     */
    public NumberingWizard(JFrame parent, CircuitComponent circuitComponent) {
        super(parent, Lang.get("msg_numberingWizard"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.circuitComponent = circuitComponent;
        label = new JLabel();
        label.setFont(Screen.getInstance().getFont(1.5f));
        int b = Screen.getInstance().getFontSize();
        label.setBorder(BorderFactory.createEmptyBorder(b, b, b, b));
        setPinNumber(999);
        getContentPane().add(label);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                circuitComponent.deactivateWizard();
            }
        });
        pack();

        pinNumber = 1;
        setPinNumber(pinNumber);
        setLocation(parent.getLocation());
    }

    private void setPinNumber(int num) {
        label.setText(Lang.get("msg_pin_numbering_N", num));
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
        if (clicked.equalsDescription(In.DESCRIPTION)
                || clicked.equalsDescription(Clock.DESCRIPTION)
                || clicked.equalsDescription(Out.DESCRIPTION)) {
            circuitComponent.modify(new ModifyAttribute<>(clicked, Keys.PINNUMBER, Integer.toString(pinNumber)));
            pinNumber++;
            setPinNumber(pinNumber);
        }
    }

    @Override
    public void closed() {
        dispose();
    }
}
