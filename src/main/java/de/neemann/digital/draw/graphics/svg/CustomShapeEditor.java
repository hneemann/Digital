/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.GraphicSwing;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.VectorFloat;
import de.neemann.digital.draw.shapes.custom.CustomShape;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription.Pin;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.AttributeDialog;
import de.neemann.digital.gui.components.ConstraintsBuilder;
import de.neemann.digital.gui.components.EditorFactory.LabelEditor;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.MyFileChooser;
import de.neemann.gui.ToolTipAction;

/**
 * @author felix
 */
public final class CustomShapeEditor extends LabelEditor<CustomShapeDescription> {

    private VPanel preview = new VPanel();
    private ImportSVG importer;
    private CustomShapeDescription svg;
    private JDialog dialog;
    private JPanel panel = new JPanel(new FlowLayout());
    private static File chooserRoot = new File(System.getProperty("user.home"));

    /**
     * Editor for the import of a SVG File
     * @param customShapeDescription
     *            Description of a custom shape
     * @param key
     *            Corresponding key
     */
    @SuppressWarnings("serial")
    public CustomShapeEditor(CustomShapeDescription customShapeDescription, Key<DataField> key) {
        this.svg = customShapeDescription;
        dialog = new JDialog(getAttributeDialog(), Lang.get("btn_load"),
                ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.add(preview, BorderLayout.CENTER);
        dialog.add(new ToolTipAction(Lang.get("btn_load")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new MyFileChooser();
                fc.setCurrentDirectory(chooserRoot);
                fc.setFileFilter(new FileNameExtensionFilter("SVG", "svg"));
                if (fc.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                    try {
                        chooserRoot = fc.getCurrentDirectory();
                        importer = new ImportSVG(fc.getSelectedFile());
                        svg = importer.getSVG();
                        preview.initPins();
                        preview.repaint();
                    } catch (Exception ex) {
                        new ErrorMessage(
                                "Beim Öffnen der SVG Datei ist ein Fehler aufgetreten (constStr)")
                                        .addCause(ex).show(panel);
                        ex.printStackTrace();
                    }
                }
            }

        }.createJButton(), BorderLayout.NORTH);
        JPanel sizeButtons = new JPanel();
        sizeButtons.setLayout(new BorderLayout());
        sizeButtons.add(new ToolTipAction(Lang.get("ok")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        }.createJButton(), BorderLayout.SOUTH);

        JButton sizeDown = new JButton("-");
        JButton sizeUp = new JButton("+");
        sizeDown.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                preview.scale(0.9);
            }
        });
        sizeUp.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                preview.scale(1.1);

            }
        });
        sizeButtons.add(sizeDown, BorderLayout.WEST);
        sizeButtons.add(sizeUp, BorderLayout.EAST);
        JButton moveUp = new JButton("ʌ");
        JButton moveDown = new JButton("v");
        JButton moveLeft = new JButton("<");
        JButton moveRight = new JButton(">");
        moveUp.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                preview.move(0.0, 0.3);
            }
        });
        moveDown.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                preview.move(0.0, -0.3);
            }
        });
        moveLeft.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                preview.move(0.3, 0.0);
            }
        });
        moveRight.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                preview.move(-0.3, 0.0);
            }
        });
        JPanel dpad = new JPanel();
        dpad.setLayout(new BorderLayout());
        dpad.add(moveUp, BorderLayout.NORTH);
        dpad.add(moveDown, BorderLayout.SOUTH);
        dpad.add(moveLeft, BorderLayout.WEST);
        dpad.add(moveRight, BorderLayout.EAST);
        sizeButtons.add(dpad, BorderLayout.CENTER);
        dialog.add(sizeButtons, BorderLayout.SOUTH);
        dialog.setSize(new Dimension(300, 300));
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    @SuppressWarnings("serial")
    @Override
    public JComponent getComponent(ElementAttributes attr) {
        panel.add(new ToolTipAction(Lang.get("btn_clearData")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                svg = CustomShapeDescription.EMPTY;
            }
        }.createJButton());
        panel.add(new ToolTipAction(Lang.get("btn_load")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // customShapeDescription=CustomShapeDescription.createDummy();
                dialog.setVisible(true);
            }
        }.createJButton());
        return panel;
    }

    private final class VPanel extends JPanel {
        private static final long serialVersionUID = -8300408021826103824L;
        private double scale = 2.0;
        private double translateX = 10;
        private double translateY = 10;
        private int lastPinX = 0;
        private int lastPinY = 0;
        private float circuitX = 0;
        private float circuitY = 0;
        private ArrayList<SVGPseudoPin> pins = new ArrayList<SVGPseudoPin>();
        private boolean drag = false;
        private int dragged;

        /**
         * Sets the fragments for displaying
         * @param fragments
         *            parts of the svg
         */
        VPanel() {
            lastPinX = 0;
            lastPinY = 0;
            scale = 2.0;
            translateX = 10;
            translateY = 10;
            this.addMouseListener(new MouseListener() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (drag) {
                        Vector fresh = getPosition(e);
                        pins.get(dragged).setPos(fresh);
                        repaint();
                        svg = svg.transformPin(fresh, pins.get(dragged).getLabel());
                    }
                    drag = false;
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    drag = false;
                    for (int i = 0; i < pins.size(); i++) {
                        Vector v = getPosition(e);
                        if (pins.get(i).contains(v.getX(), v.getY())) {
                            drag = true;
                            dragged = i;
                            break;
                        }
                    }
                    VectorFloat pos = getPositionFloat(e);
                    circuitX = pos.getXFloat();
                    circuitY = pos.getYFloat();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                }
            });
            this.addMouseMotionListener(new MouseMotionListener() {

                @Override
                public void mouseMoved(MouseEvent e) {
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (drag) {
                        pins.get(dragged).setPos(getPosition(e));
                        repaint();
                    } else {
                        VectorFloat pos = getPositionFloat(e);
                        float diffX = circuitX - pos.getXFloat();
                        float diffY = circuitY - pos.getYFloat();
                        move(diffX, diffY);
                        circuitX = pos.getXFloat();
                        circuitY = pos.getYFloat();
                    }

                }
            });
            this.addMouseWheelListener(new MouseWheelListener() {

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    double backup = scale;
                    scale = scale - 0.05 * e.getWheelRotation();
                    if (scale < 0)
                        scale = backup;
                    repaint();
                }
            });
            initPins();
            repaint();
        }

        public void scale(double faktor) {
            if (importer != null) {
                for (SVGFragment f : importer.getFragments()) {
                    if (f != null) {
                        f.scale(faktor);
                    }
                }
            }
            preview.repaint();
        }

        public void move(double v, double h) {
            if (importer != null) {
                for (SVGFragment f : importer.getFragments()) {
                    if (f != null) {
                        f.move(new VectorFloat((float) v, (float) h));
                    }
                }
            }
            preview.repaint();
        }

        private Vector getPosition(MouseEvent e) {
            VectorFloat v = getPositionFloat(e);
            return new Vector(v.getX(), v.getY());
        }

        private VectorFloat getPositionFloat(MouseEvent e) {
            return new VectorFloat((float) ((e.getX() - translateX) / scale),
                    (float) ((e.getY() - translateY) / scale));
        }

        public void initPins() {
            pins = new ArrayList<SVGPseudoPin>();
            if (svg != null) {
                for (String s : svg.getPinNames().keySet()) {
                    Pin p = svg.getPinNames().get(s);
                    if (!isPinPresent(s))
                        pins.add(new SVGPseudoPin(p.getPos(), s, true, null));
                }
            }
            if (getAttributeDialog() != null) {
                Window p = getAttributeDialog().getDialogParent();
                if (p instanceof Main) {
                    Circuit c = ((Main) p).getCircuitComponent().getCircuit();
                    for (VisualElement ve : c.getElements()) {
                        if (ve.equalsDescription(In.DESCRIPTION)
                                || ve.equalsDescription(Clock.DESCRIPTION)) {
                            String label = ve.getElementAttributes().getLabel();
                            addPin(true, label);
                        } else if (ve.equalsDescription(Out.DESCRIPTION)) {
                            String label = ve.getElementAttributes().getLabel();
                            addPin(false, label);
                        }
                    }
                }
            }
        }

        /**
         * Moves the circuit, for the first Pin to be on 0/0
         */
        private void applyToZero() {
            if (pins.size() > 0 && pins.get(0) != null) {
                int diffX = pins.get(0).getPos().getX();
                int diffY = pins.get(0).getPos().getY();
                for (SVGFragment f : importer.getFragments()) {
                    if (f != null) {
                        f.move(new VectorFloat(diffX, diffY));
                    }
                }
                for (SVGPseudoPin p : pins) {
                    if (p != null) {
                        p.move(new VectorFloat(diffX, diffY));
                    }
                }
            }
            if (importer != null) {
                importer.setPins(pins);
                svg = importer.getSVG();
            }
        }

        /**
         * Adds a new Pin
         * @param input
         *            Input or output
         */
        private void addPin(boolean input, String label) {
            if (!isPinPresent(label)) {
                svg = svg.addPin(label, new Vector(lastPinX, lastPinY), true);
                while (isPinOnPosition(new Vector(lastPinX, lastPinY)) > 0) {
                    lastPinX += 20;
                }
                SVGPseudoPin pseudoPin = new SVGPseudoPin(new Vector(lastPinX, lastPinY), label,
                        input, null);
                pins.add(pseudoPin);
                lastPinY += 20;
                if (lastPinY > 150) {
                    lastPinY = 0;
                    lastPinX += 20;
                }
            }
            repaint();
        }

        /**
         * Counts the Pins on Position pos
         * @param pos
         *            Position
         * @return Number of Pins on this Position
         */
        private int isPinOnPosition(Vector pos) {
            int ret = 0;
            for (SVGPseudoPin p : pins) {
                if (p.getPos().equals(pos))
                    ret++;
            }
            return ret;
        }

        /**
         * Checks, if a Pin is already on stage
         * @param label
         *            Name of the Pin
         * @return true or false
         */
        private boolean isPinPresent(String label) {
            for (SVGPseudoPin pin : pins) {
                if (pin.getLabel().equals(label)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (!drag)
                applyToZero();
            Graphics2D g2d = (Graphics2D) g;
            translateX = getWidth() / 2;
            translateY = getHeight() / 2;
            g2d.translate(translateX, translateY);
            g2d.scale(scale, scale);
            GraphicSwing graphic = new GraphicSwing(g2d);
            g2d.drawLine(0, 1000, 0, -1000);
            g2d.drawLine(1000, 0, -1000, 0);
            try {
                new CustomShape(svg, null, null).drawTo(graphic, null);
            } catch (PinException e1) {
                e1.printStackTrace();
            }
            for (SVGPseudoPin p : pins) {
                if (p != null) {
                    for (SVGDrawable d : p.getDrawables()) {
                        d.draw(graphic);
                        SVGEllipse e = (SVGEllipse) d;
                        graphic.drawText(ImportSVG.toOldschoolVector(e.getPos()),
                                ImportSVG.toOldschoolVector(e.getPos()), p.getLabel(),
                                p.isInput() ? Orientation.RIGHTTOP : Orientation.LEFTTOP,
                                Style.NORMAL.deriveFontStyle(12, true)
                                        .deriveFillStyle(p.isInput() ? Color.blue : Color.red));
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void addToPanel(JPanel panel, Key key, ElementAttributes elementAttributes,
            AttributeDialog attributeDialog, ConstraintsBuilder constraints) {
        super.addToPanel(panel, key, elementAttributes, attributeDialog, constraints);
    }

    @Override
    public CustomShapeDescription getValue() {
        return svg;
    }

    @Override
    public void setValue(CustomShapeDescription value) {
        // TODO Auto-generated method stub

    }
}
