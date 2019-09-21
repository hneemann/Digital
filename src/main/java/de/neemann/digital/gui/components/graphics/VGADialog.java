/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The dialog used to show the VGA screen
 */
public class VGADialog extends JDialog {
    private final MyComponent graphicComponent;

    /**
     * Creates a new instance of the given size
     *
     * @param parent the parent window
     * @param title  the window title
     * @param image  the image data
     */
    public VGADialog(Window parent, String title, BufferedImage image) {
        super(parent, title, ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        graphicComponent = new MyComponent(image);
        getContentPane().add(graphicComponent);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);

        addWindowFocusListener(new MoveFocusTo(parent));
    }

    /**
     * Updates the graphics data
     */
    public void updateGraphic() {
        graphicComponent.repaint();
    }

    private static final class MyComponent extends JComponent {
        private final BufferedImage image;

        private MyComponent(BufferedImage image) {
            super();
            this.image = image;
            setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.drawImage(image, 0, 0, null);
        }
    }
}
