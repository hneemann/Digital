/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.draw.graphics.ColorKey;
import de.neemann.digital.draw.graphics.ColorScheme;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Editor for color schemes
 */
public class ColorSchemeEditor extends EditorFactory.LabelEditor<ColorScheme> {

    private ColorScheme colorScheme;

    /**
     * Creates a new instance
     *
     * @param colorScheme the color scheme
     * @param key         the key used
     */
    public ColorSchemeEditor(ColorScheme colorScheme, Key<ColorScheme> key) {
        this.colorScheme = colorScheme;
    }

    @Override
    public ColorScheme getValue() {
        return colorScheme;
    }

    @Override
    public void setValue(ColorScheme value) {
        this.colorScheme = value;
    }


    @Override
    protected JComponent getComponent(ElementAttributes elementAttributes) {
        return new JButton(new AbstractAction(Lang.get("btn_edit")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new SchemeEditor(colorScheme).setVisible(true);
            }
        });
    }

    private final class SchemeEditor extends JDialog {
        private final ColorScheme.Builder builder;
        private final ArrayList<ColorButton> colorButtons;

        private SchemeEditor(ColorScheme colorScheme) {
            super((Frame) null, Lang.get("key_customColorScheme"), true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            builder = new ColorScheme.Builder(colorScheme);
            colorButtons = new ArrayList<>();
            JPanel colors = new JPanel(new GridLayout(ColorKey.values().length, 1));
            for (ColorKey ck : ColorKey.values()) {
                ColorButton colorButton = new ColorButton(builder, ck);
                colorButtons.add(colorButton);
                colors.add(colorButton);
            }
            getContentPane().add(colors);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.add(new ToolTipAction(Lang.get("cancel")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    dispose();
                }
            }.createJButton());
            buttons.add(new ToolTipAction(Lang.get("ok")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    setValue(builder.build());
                    dispose();
                }
            }.createJButton());

            JMenu preset = new JMenu(Lang.get("menu_colorSchemePreset"));
            for (ColorScheme.ColorSchemes cs : ColorScheme.ColorSchemes.values())
                if (!cs.equals(ColorScheme.ColorSchemes.CUSTOM))
                    preset.add(new PresetAction(cs).createJMenuItem());
            JMenuBar bar = new JMenuBar();
            bar.add(preset);
            setJMenuBar(bar);

            getContentPane().add(buttons, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(null);
        }

        private final class PresetAction extends ToolTipAction {
            private final ColorScheme.ColorSchemes cs;

            private PresetAction(ColorScheme.ColorSchemes cs) {
                super(Lang.get("key_colorScheme_" + cs.name()));
                this.cs = cs;
            }

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                builder.set(cs.getScheme());
                for (ColorButton cb : colorButtons)
                    cb.updateColor();
            }
        }
    }

    private static final class ColorButton extends JButton {
        private final ColorScheme.Builder builder;
        private final ColorKey ck;

        private ColorButton(ColorScheme.Builder builder, ColorKey ck) {
            super(Lang.get("colorName_" + ck.name()));
            this.builder = builder;
            this.ck = ck;
            setColor(builder.getColor(ck));

            addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    Color color = JColorChooser.showDialog(ColorButton.this, Lang.get("msg_color"), builder.getColor(ck));
                    if (color != null) {
                        builder.set(ck, color);
                        setColor(color);
                    }
                }
            });
        }

        private void updateColor() {
            setColor(builder.getColor(ck));
        }

        private void setColor(Color color) {
            setBackground(color);
            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            if (hsb[2] < 0.7)
                setForeground(Color.WHITE);
            else
                setForeground(Color.BLACK);
        }

    }

}
