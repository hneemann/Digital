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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

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
import de.neemann.digital.draw.shapes.custom.CustomShape;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
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
                fc.setFileFilter(new FileNameExtensionFilter("SVG", "svg"));
                if (fc.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                    try {
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
        dialog.add(new ToolTipAction(Lang.get("ok")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        }.createJButton(), BorderLayout.SOUTH);
        dialog.setSize(new Dimension(300, 300));
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

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
        private double scale = 2.0;
        private double translateX = 10;
        private double translateY = 10;
        private int lastPinX = 0;
        private int lastPinY = 0;
        private ArrayList<SVGPseudoPin> pins = new ArrayList<SVGPseudoPin>();
        private boolean drag = false;
        private int dragged;

        /**
         * Sets the fragments for displaying
         * @param fragments
         *            parts of the svg
         */
        public VPanel() {
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
                        try {
                            svg = svg.transformPin(fresh, pins.get(dragged).getLabel());
                        } catch (PinException e1) {
                            e1.printStackTrace();
                        }
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

        private Vector getPosition(MouseEvent e) {
            return new Vector((int) ((e.getX() - translateX) / scale),
                    (int) ((e.getY() - translateY) / scale));
        }

        public void initPins() {
            pins = new ArrayList<SVGPseudoPin>();
            if (svg != null) {
                for (SVGPseudoPin p : svg.getPinNames()) {
                    if (!isPinPresent(p.getLabel()))
                        pins.add(p);
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
                        f.move(new Vector(diffX, diffY));
                    }
                }
                for (SVGPseudoPin p : pins) {
                    if (p != null) {
                        p.move(new Vector(diffX, diffY));
                    }
                }
            }
            if (importer != null) {
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
                svg = svg.addPin(label, new Vector(lastPinX, lastPinY), input);
                while (isPinOnPosition(new Vector(lastPinX, lastPinY)) > 0) {
                    lastPinY += 20;
                }
                SVGPseudoPin pseudoPin = new SVGPseudoPin(new Vector(lastPinX, lastPinY), label,
                        input, null);
                pins.add(pseudoPin);
                lastPinX += 20;
                if (lastPinX > 150) {
                    lastPinX = 0;
                    lastPinY += 20;
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
                        graphic.drawText(e.getPos(), e.getPos(), p.getLabel(),
                                p.isInput() ? Orientation.RIGHTTOP : Orientation.LEFTTOP,
                                Style.NORMAL.deriveFontStyle(12, true)
                                        .deriveFillStyle(p.isInput() ? Color.blue : Color.red));
                    }
                }
            }
        }
    }

    @Override
    public void addToPanel(JPanel panel, Key key, ElementAttributes elementAttributes,
            AttributeDialog attributeDialog, ConstraintsBuilder constraints) {
        super.addToPanel(panel, key, elementAttributes, attributeDialog, constraints);
        // if (svg.isSet()) {
        // try {
        // ImportSVG importer = new ImportSVG(svg, null, null);
        // preview.setSVG(importer.getFragments());
        // } catch (NoParsableSVGException e1) {
        // new ErrorMessage("Beim Öffnen der SVG Datei ist ein Fehler aufgetreten
        // (constStr)").addCause(e1)
        // .show(panel);
        // e1.printStackTrace();
        // }
        // }
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
