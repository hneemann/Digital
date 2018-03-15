/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.core.Bits;
import de.neemann.digital.core.IntFormat;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.arithmetic.BarrelShifterMode;
import de.neemann.digital.core.arithmetic.LeftRightFormat;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.element.Rotation;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.InValue;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.core.memory.rom.ROMManger;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.GraphicSwing;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.svg.ImportSVG;
import de.neemann.digital.draw.graphics.svg.SVGDrawable;
import de.neemann.digital.draw.graphics.svg.SVGEllipse;
import de.neemann.digital.draw.graphics.svg.SVGPseudoPin;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.custom.CustomShape;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.table.ShowStringDialog;
import de.neemann.digital.gui.components.testing.TestCaseDescriptionEditor;
import de.neemann.digital.gui.sync.NoSync;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.LineBreaker;
import de.neemann.gui.MyFileChooser;
import de.neemann.gui.Screen;
import de.neemann.gui.ToolTipAction;
import de.neemann.gui.language.Bundle;
import de.neemann.gui.language.Language;

/**
 */
public final class EditorFactory {

    /**
     * The single EditorFactory instance.
     */
    public static final EditorFactory INSTANCE = new EditorFactory();
    private HashMap<Class<?>, Class<? extends Editor>> map = new HashMap<>();

    private EditorFactory() {
        add(String.class, StringEditor.class);
        add(Integer.class, IntegerEditor.class);
        add(Long.class, LongEditor.class);
        add(InValue.class, InValueEditor.class);
        add(File.class, FileEditor.class);
        add(CustomShapeDescription.class, CustomShapeEditor.class);
        add(Color.class, ColorEditor.class);
        add(Boolean.class, BooleanEditor.class);
        add(DataField.class, DataFieldEditor.class);
        add(Rotation.class, RotationEditor.class);
        add(BarrelShifterMode.class, BarrelShifterModeEditor.class);
        add(LeftRightFormat.class, LeftRightFormatsEditor.class);
        add(IntFormat.class, IntFormatsEditor.class);
        add(Language.class, LanguageEditor.class);
        add(TestCaseDescription.class, TestCaseDescriptionEditor.class);
        add(FormatToExpression.class, FormatEditor.class);
        add(InverterConfig.class, InverterConfigEditor.class);
        add(ROMManger.class, ROMManagerEditor.class);
    }

    private <T> void add(Class<T> clazz, Class<? extends Editor<T>> editor) {
        map.put(clazz, editor);
    }

    /**
     * Creates a new Editor
     * @param key
     *            the key
     * @param value
     *            the value
     * @param <T>
     *            the type of the value
     * @return the editor
     */
    public <T> Editor<T> create(Key<T> key, T value) {
        Class<? extends Editor> fac = map.get(key.getValueClass());
        if (fac == null)
            throw new RuntimeException("no editor found for " + key.getValueClass().getSimpleName());

        try {
            Constructor<? extends Editor> c = fac.getConstructor(value.getClass(), Key.class);
            return c.newInstance(value, key);
        } catch (Exception e) {
            throw new RuntimeException("error creating editor", e);
        }
    }

    /**
     * Simple single component editor
     * @param <T>
     *            the type to edit
     */
    public static abstract class LabelEditor<T> implements Editor<T> {
        private AttributeDialog attributeDialog;
        private boolean labelAtTop = false;
        private JComponent component;
        private JLabel label;

        @Override
        public void addToPanel(JPanel panel, Key key, ElementAttributes elementAttributes,
                AttributeDialog attributeDialog, ConstraintsBuilder constraints) {
            this.attributeDialog = attributeDialog;
            label = new JLabel(key.getName() + ":  ");
            final String description = new LineBreaker().toHTML().breakLines(key.getDescription());
            label.setToolTipText(description);
            component = getComponent(elementAttributes);
            component.setToolTipText(description);
            if (labelAtTop) {
                panel.add(label, constraints.width(2));
                constraints.nextRow();
                panel.add(component, constraints.width(2).dynamicHeight());
            } else {
                panel.add(label, constraints);
                panel.add(component, constraints.x(1).dynamicWidth());
            }
        }

        /**
         * @return the containing dialog
         */
        public AttributeDialog getAttributeDialog() {
            return attributeDialog;
        }

        /**
         * returns the editor component
         * @param elementAttributes
         *            the elements attributes
         * @return the component
         */
        protected abstract JComponent getComponent(ElementAttributes elementAttributes);

        @Override
        public void setEnabled(boolean enabled) {
            label.setEnabled(enabled);
            component.setEnabled(enabled);
        }

        /**
         * Sets the position of the label
         * @param labelAtTop
         *            if true the label is placed at the top of the editing component.
         */
        void setLabelAtTop(boolean labelAtTop) {
            this.labelAtTop = labelAtTop;
        }
    }

    // Checkstyle flags redundant modifiers, which are not redundant. Maybe a bug in
    // checkstyle?
    // CHECKSTYLE.OFF: RedundantModifier
    final static class StringEditor extends LabelEditor<String> {

        private final JTextComponent text;
        private final JComponent compToAdd;

        public StringEditor(String value, Key<String> key) {
            if (key instanceof Key.LongString) {
                text = new JTextArea(6, 30);
                compToAdd = new JScrollPane(text);
                setLabelAtTop(true);
            } else {
                text = new JTextField(10);
                compToAdd = text;
            }
            text.setText(value);
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return compToAdd;
        }

        @Override
        public String getValue() {
            return text.getText().trim();
        }

        public JTextComponent getTextComponent() {
            return text;
        }
    }

    private final static class IntegerEditor extends LabelEditor<Integer> {
        private static final Integer[] DEFAULTS = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
        };
        private final JComboBox<Integer> comboBox;
        private final Key<Integer> key;

        public IntegerEditor(Integer value, Key<Integer> key) {
            this.key = key;
            Integer[] selects = null;
            if (key instanceof Key.KeyInteger) {
                selects = ((Key.KeyInteger) key).getComboBoxValues();
            }
            if (selects == null)
                selects = DEFAULTS;
            comboBox = new JComboBox<>(selects);
            comboBox.setEditable(true);
            comboBox.setSelectedItem(value);
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return comboBox;
        }

        @Override
        public Integer getValue() throws EditorParseException {
            Object item = comboBox.getSelectedItem();
            int value = 0;
            if (item instanceof Number)
                value = ((Number) item).intValue();
            else {
                try {
                    value = (int) Bits.decode(item.toString());
                } catch (Bits.NumberFormatException e) {
                    throw new EditorParseException(e);
                }
            }

            if (key instanceof Key.KeyInteger) {
                int min = ((Key.KeyInteger) key).getMin();
                if (value < min)
                    value = min;
                int max = ((Key.KeyInteger) key).getMax();
                if (value > max)
                    value = max;
            }

            return value;
        }
    }

    private final static class LongEditor extends LabelEditor<Long> {
        private static final Long[] DEFAULTS = {
                0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L
        };
        private final JComboBox<Long> comboBox;

        public LongEditor(Long value, Key<Long> key) {
            comboBox = new JComboBox<>(DEFAULTS);
            comboBox.setEditable(true);
            comboBox.setSelectedItem(value.toString());
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return comboBox;
        }

        @Override
        public Long getValue() {
            Object item = comboBox.getSelectedItem();
            long value = 0;
            if (item instanceof Number)
                value = ((Number) item).longValue();
            else {
                try {
                    value = Bits.decode(item.toString());
                } catch (Bits.NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
            return value;
        }
    }

    private final static class InValueEditor extends LabelEditor<InValue> {
        private static final String[] DEFAULTS = {
                "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"
        };
        private final JComboBox<String> comboBox;

        public InValueEditor(InValue value, Key<Integer> key) {
            comboBox = new JComboBox<>(DEFAULTS);
            comboBox.setEditable(true);
            comboBox.setSelectedItem(value.toString());
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return comboBox;
        }

        @Override
        public InValue getValue() throws EditorParseException {
            Object item = comboBox.getSelectedItem();
            try {
                return new InValue(item.toString());
            } catch (Bits.NumberFormatException e) {
                throw new EditorParseException(e);
            }
        }
    }

    final static class BooleanEditor implements Editor<Boolean> {

        private final JCheckBox bool;

        public BooleanEditor(Boolean value, Key<Boolean> key) {
            bool = new JCheckBox(key.getName(), value);
            bool.setToolTipText(new LineBreaker().toHTML().breakLines(key.getDescription()));
        }

        @Override
        public Boolean getValue() {
            return bool.isSelected();
        }

        @Override
        public void addToPanel(JPanel panel, Key key, ElementAttributes elementAttributes,
                AttributeDialog attributeDialog, ConstraintsBuilder constraints) {
            panel.add(bool, constraints.width(2));
        }

        @Override
        public void setEnabled(boolean enabled) {
            bool.setEnabled(enabled);
        }

        JCheckBox getCheckBox() {
            return bool;
        }
    }

    private final static class ColorEditor extends LabelEditor<Color> {

        private Color color;
        private final JButton button;

        public ColorEditor(Color value, Key<Color> key) {
            this.color = value;
            button = new JButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Color col = JColorChooser.showDialog(button, Lang.get("msg_color"), color);
                    if (col != null) {
                        color = col;
                        button.setBackground(color);
                    }
                }
            }) {
                @Override
                protected void paintComponent(Graphics graphics) {
                    graphics.setColor(Color.WHITE);
                    graphics.fillRect(0, 0, getWidth(), getHeight());
                    super.paintComponent(graphics);
                }
            };
            button.setPreferredSize(new Dimension(10, Screen.getInstance().getFontSize() * 3 / 2));
            button.setBackground(color);
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return button;
        }

        @Override
        public Color getValue() {
            return color;
        }
    }

    /**
     * @author felix
     */
    private final static class CustomShapeEditor extends LabelEditor<CustomShapeDescription> {

        private VPanel preview = new VPanel();
        private ImportSVG importer;
        private CustomShapeDescription svg;
        private JDialog dialog;
        private JPanel panel = new JPanel(new FlowLayout());

        public CustomShapeEditor(CustomShapeDescription customShapeDescription, Key<DataField> key) {
            this.svg = customShapeDescription;
            dialog = new JDialog(getAttributeDialog(), Lang.get("btn_load"), ModalityType.APPLICATION_MODAL);
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
                            new ErrorMessage("Beim Öffnen der SVG Datei ist ein Fehler aufgetreten (constStr)")
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
                            if (ve.equalsDescription(In.DESCRIPTION) || ve.equalsDescription(Clock.DESCRIPTION)) {
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
                    SVGPseudoPin pseudoPin = new SVGPseudoPin(new Vector(lastPinX, lastPinY), label, input, null);
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
                            Vector fresh = new Vector((int) (e.getX() / scale - translateX),
                                    (int) (e.getY() / scale - translateY));
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
                            if (pins.get(i).contains((int) (e.getX() / scale - translateX),
                                    (int) (e.getY() / scale - translateY))) {
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
                            pins.get(dragged).setPos(new Vector((int) (e.getX() / scale - translateX),
                                    (int) (e.getY() / scale - translateY)));
                            repaint();
                        }
                    }
                });
                this.addMouseWheelListener(new MouseWheelListener() {

                    @Override
                    public void mouseWheelMoved(MouseWheelEvent e) {
                        scale = scale - 0.1 * e.getWheelRotation();
                        repaint();
                    }
                });
                initPins();
                repaint();
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.scale(scale, scale);
                g2d.translate(translateX, translateY);
                GraphicSwing graphic = new GraphicSwing(g2d);
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
                            graphic.drawText(e.getPos(), e.getPos(), "(" + p.getLabel() + ")",
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
    }

    private final static class FileEditor extends LabelEditor<File> {

        private final JPanel panel;
        private final JTextField textField;
        private final boolean directoryOnly;

        public FileEditor(File value, Key<File> key) {
            if (key instanceof Key.KeyFile)
                directoryOnly = ((Key.KeyFile) key).isDirectoryOnly();
            else
                directoryOnly = false;

            panel = new JPanel(new BorderLayout());
            textField = new JTextField(value.getPath(), 20);
            JButton button = new JButton(new AbstractAction("...") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fc = new MyFileChooser(FileEditor.this.getValue());
                    if (directoryOnly)
                        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (fc.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION)
                        textField.setText(fc.getSelectedFile().getPath());
                }
            });
            panel.add(textField, BorderLayout.CENTER);
            panel.add(button, BorderLayout.EAST);
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return panel;
        }

        @Override
        public File getValue() {
            return new File(textField.getText());
        }
    }

    private final static class DataFieldEditor extends LabelEditor<DataField> {

        private DataField data;

        public DataFieldEditor(DataField data, Key<DataField> key) {
            this.data = data;
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(new ToolTipAction(Lang.get("btn_edit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        getAttributeDialog().storeEditedValues();
                        int dataBits = attr.get(Keys.BITS);
                        int addrBits;

                        // INPUT_COUNT and ADDR_BITS must have the same default value!!!
                        // If INPUT_COUNT is not present (default value is used) the default value of
                        // ADDR_BITS is used. This works only if both have the same default value!!!
                        if (attr.contains(Keys.INPUT_COUNT)) {
                            // used to handle the LUT
                            addrBits = attr.get(Keys.INPUT_COUNT);
                        } else {
                            // memory, RAM/ROM
                            addrBits = attr.get(Keys.ADDR_BITS);
                        }
                        int size = 1 << addrBits;
                        DataEditor de = new DataEditor(panel, data, size, dataBits, addrBits, false, NoSync.INST);
                        de.setFileName(attr.getFile(ROM.LAST_DATA_FILE_KEY));
                        if (de.showDialog()) {
                            data = de.getModifiedDataField();
                            attr.setFile(ROM.LAST_DATA_FILE_KEY, de.getFileName());
                        }
                    } catch (EditorParseException e1) {
                        new ErrorMessage(Lang.get("msg_invalidEditorValue")).addCause(e1).show(panel);
                    }
                }
            }.createJButton());
            panel.add(new ToolTipAction(Lang.get("btn_reload")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                data = new DataField(attr.getFile(ROM.LAST_DATA_FILE_KEY));
                            } catch (IOException e1) {
                                new ErrorMessage(Lang.get("msg_errorReadingFile")).addCause(e1).show(panel);
                            }
                        }
                    }
                            .setEnabledChain(attr.getFile(ROM.LAST_DATA_FILE_KEY) != null)
                            .setToolTip(Lang.get("btn_reload_tt"))
                            .createJButton()
            );
            return panel;
        }

        @Override
        public DataField getValue() {
            return data.getMinimized();
        }
    }

    private final static class RotationEditor extends LabelEditor<Rotation> {
        private static final String[] LIST = new String[]{Lang.get("rot_0"), Lang.get("rot_90"), Lang.get("rot_180"), Lang.get("rot_270")};

        private final Rotation rotation;
        private JComboBox<String> comb;

        public RotationEditor(Rotation rotation, Key<Rotation> key) {
            this.rotation = rotation;
        }

        @Override
        public JComponent getComponent(ElementAttributes elementAttributes) {
            comb = new JComboBox<>(LIST);
            comb.setSelectedIndex(rotation.getRotation());
            return comb;
        }

        @Override
        public Rotation getValue() {
            return new Rotation(comb.getSelectedIndex());
        }
    }

    private static class EnumEditor<E extends Enum> extends LabelEditor<E> {
        private final JComboBox comboBox;
        private final E[] values;
        private final String[] names;

        public EnumEditor(Enum value, Key<E> key) {
            if (!(key instanceof Key.KeyEnum))
                throw new RuntimeException("wrong enum type");
            this.names = ((Key.KeyEnum<E>) key).getNames();
            this.values = ((Key.KeyEnum<E>) key).getValues();

            comboBox = new JComboBox<>(names);
            comboBox.setSelectedIndex(value.ordinal());
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            return comboBox;
        }

        @Override
        public E getValue() {
            return values[comboBox.getSelectedIndex()];
        }
    }

    private static final class IntFormatsEditor extends EnumEditor<IntFormat> {
        public IntFormatsEditor(IntFormat value, Key<IntFormat> key) {
            super(value, key);
        }
    }

    private static final class BarrelShifterModeEditor extends EnumEditor<BarrelShifterMode> {
        public BarrelShifterModeEditor(BarrelShifterMode value, Key<BarrelShifterMode> key) {
            super(value, key);
        }
    }

    private static final class LeftRightFormatsEditor extends EnumEditor<LeftRightFormat> {
        public LeftRightFormatsEditor(LeftRightFormat value, Key<LeftRightFormat> key) {
            super(value, key);
        }
    }

    private static class LanguageEditor extends LabelEditor<Language> {
        private JComboBox comb;

        public LanguageEditor(Language language, Key<Rotation> key) {
            Bundle b = Lang.getBundle();
            List<Language> supLang = b.getSupportedLanguages();
            comb = new JComboBox<>(supLang.toArray(new Language[supLang.size()]));
            comb.setSelectedItem(Lang.currentLanguage());
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            return comb;
        }

        @Override
        public Language getValue() {
            return (Language) comb.getSelectedItem();
        }
    }

    private static class FormatEditor extends LabelEditor<FormatToExpression> {
        private JComboBox comb;

        public FormatEditor(FormatToExpression format, Key<Rotation> key) {
            FormatToExpression[] formats = FormatToExpression.getAvailFormats();
            comb = new JComboBox<>(formats);
            comb.setSelectedItem(format);
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            return comb;
        }

        @Override
        public FormatToExpression getValue() {
            return (FormatToExpression) comb.getSelectedItem();
        }
    }

    private static class InverterConfigEditor extends LabelEditor<InverterConfig> {

        private final JButton button;
        private InverterConfig inverterConfig;
        private ElementAttributes elementAttributes;

        public InverterConfigEditor(InverterConfig aInverterConfig, Key<InverterConfig> key) {
            this.inverterConfig = aInverterConfig;
            button = new JButton(new ToolTipAction(getButtonText()) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    VisualElement ve = getAttributeDialog().getVisualElement();
                    Window p = getAttributeDialog().getDialogParent();
                    if (ve != null && p instanceof Main) {
                        try {
                            getAttributeDialog().storeEditedValues();
                            ElementTypeDescription d = ((Main) p).getCircuitComponent().getLibrary()
                                    .getElementType(ve.getElementName());
                            PinDescriptions in = d.getInputDescription(elementAttributes);
                            InputSelectDialog dialog = new InputSelectDialog(getAttributeDialog(), in, inverterConfig);
                            if (dialog.showDialog()) {
                                inverterConfig = dialog.getInverterConfig();
                                button.setText(getButtonText());
                            }
                        } catch (ElementNotFoundException | NodeException e) {
                            new ErrorMessage(Lang.get("msg_errGettingPinNames")).addCause(e).show(getAttributeDialog());
                        } catch (EditorParseException e) {
                            new ErrorMessage(Lang.get("msg_invalidEditorValue")).addCause(e).show(getAttributeDialog());
                        }
                    }
                }
            });
        }

        private String getButtonText() {
            if (inverterConfig.isEmpty())
                return Lang.get("msg_none");
            return inverterConfig.toString();
        }

        @Override
        public InverterConfig getValue() {
            return inverterConfig;
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            this.elementAttributes = elementAttributes;
            return button;
        }
    }

    private final static class InputSelectDialog extends JDialog {
        private final ArrayList<JCheckBox> boxes;
        private boolean ok = false;

        private InputSelectDialog(JDialog parent, PinDescriptions pins, InverterConfig inverterConfig) {
            super(parent, Lang.get("msg_inputsToInvert"), true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            boxes = new ArrayList<>();
            for (PinDescription p : pins) {
                JCheckBox cb = new JCheckBox(p.getName());
                cb.setSelected(inverterConfig.contains(p.getName()));
                boxes.add(cb);
                panel.add(cb);
            }
            int pad = Screen.getInstance().getFontSize();
            panel.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
            getContentPane().add(panel);
            getContentPane().add(new JButton(new AbstractAction(Lang.get("ok")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    ok = true;
                    dispose();
                }
            }), BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(parent);
        }

        public boolean showDialog() {
            setVisible(true);
            return ok;
        }

        private InverterConfig getInverterConfig() {
            InverterConfig ic = new InverterConfig();
            for (JCheckBox cb : boxes) {
                if (cb.isSelected())
                    ic.add(cb.getText());
            }
            return ic;
        }
    }

    private static class ROMManagerEditor extends LabelEditor<ROMManger> {
        private final JPanel buttons;
        private ROMManger romManager;

        public ROMManagerEditor(ROMManger aRomManager, Key<ROMManger> key) {
            this.romManager = aRomManager;
            buttons = new JPanel(new GridLayout(1, 2));
            buttons.add(new ToolTipAction(Lang.get("btn_help")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    new ShowStringDialog(getAttributeDialog(), Lang.get("win_romDialogHelpTitle"),
                            Lang.get("msg_romDialogHelp"), true).setVisible(true);
                }
            }.createJButton());
            buttons.add(new ToolTipAction(Lang.get("btn_edit")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    Main main = getAttributeDialog().getMain();
                    if (main != null) {
                        final ROMEditorDialog romEditorDialog;
                        try {
                            CircuitComponent circuitComponent = main.getCircuitComponent();
                            Model model = new ModelCreator(circuitComponent.getCircuit(), circuitComponent.getLibrary())
                                    .createModel(false);

                            romEditorDialog = new ROMEditorDialog(getAttributeDialog(), model, romManager);
                            if (romEditorDialog.showDialog())
                                romManager = romEditorDialog.getROMManager();
                        } catch (ElementNotFoundException | PinException | NodeException e) {
                            new ErrorMessage(Lang.get("msg_errorCreatingModel")).addCause(e).show(getAttributeDialog());
                        }
                    }
                }
            }.createJButton());
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            return buttons;
        }

        @Override
        public ROMManger getValue() {
            return romManager;
        }
    }
}
