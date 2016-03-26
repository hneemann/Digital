package de.neemann.digital.gui.components;

import java.awt.*;
import java.util.ArrayList;

/**
 * Layout manager used in teh GUI dialog
 *
 * @author hneemann
 */
public class DialogLayout implements LayoutManager {
    /**
     * Used to indicate the element as a label
     */
    public static final String LABEL = "label";
    /**
     * Used to indicate the element as a label
     */
    public static final String INPUT = "input";
    /**
     * Used to indicate that the element should use the space for label and input (e.g. CheckBoxes)
     */
    public static final String BOTH = "both";
    /**
     * Use label and input column, but indent field
     */
    public static final String BOTHINDENT = "indent";
    /**
     * Use label and input column, height is adjustable
     */
    public static final String BOTHDYNAMIC = "bothDyn";
    private static final int BOTHOFFSET = 20;
    private static final int SPACEX = 5;
    private static final int SPACEY = 5;
    private ArrayList<Line> lines;
    private int label;
    private boolean initialized = false;
    private int dynCount;

    /**
     * @return true if the are fields with
     */
    public boolean isDynamic() {
        initialize();
        return dynCount > 0;
    }

    private static class Line {
        private boolean both;
        private Component label;
        private Component input;
        private boolean indent;
        private boolean dynamic;

        public Line(Component both, boolean indent) {
            this.label = both;
            this.both = true;
            this.indent = indent;
        }

        public Line(Component component, String s) {
            both = false;
            if (LABEL.equals(s)) label = component;
            else if (INPUT.equals(s)) input = component;
            else throw new IllegalArgumentException();
        }

        public Component getLabel() {
            return label;
        }

        public Component getInput() {
            return input;
        }

        public void setLabel(Component component) {
            label = component;
        }

        public boolean isLabelFree() {
            return !both && label == null;
        }

        public boolean isInputFree() {
            return !both && input == null;
        }

        public void setInput(Component input) {
            this.input = input;
        }

        public boolean isBoth() {
            return both;
        }

        public int getHeight() {
            if (both) return label.getPreferredSize().height;
            else {
                int height = 0;
                if (label != null) {
                    int h = label.getPreferredSize().height;
                    if (h > height) height = h;
                }
                if (input != null) {
                    int h = input.getPreferredSize().height;
                    if (h > height) height = h;
                }
                return height;
            }
        }

        public boolean isIndent() {
            return indent;
        }

        public void setDynamic(boolean dynamic) {
            this.dynamic = dynamic;
        }

        public boolean isDynamic() {
            return dynamic;
        }

        public void initialize() {
            if (both) {
                if (label instanceof Container)
                    initContainer((Container) label);
            }
        }

        private void initContainer(Container container) {
            for (int i = 0; i < container.getComponentCount(); i++) {
                Component c = container.getComponent(i);
                if (c instanceof Container /*&& !(((Container)c).getLayout() instanceof DialogLayout)*/)
                    initContainer((Container) c);
            }


            LayoutManager lm = container.getLayout();
            if (lm instanceof DialogLayout) {
                DialogLayout dl = (DialogLayout) lm;
                if (dl.isDynamic()) setDynamic(true);
            }
        }
    }

    /**
     * Creates a new Layout for the edit dialog
     */
    public DialogLayout() {
        lines = new ArrayList<Line>();
    }


    @Override
    public void addLayoutComponent(String s, Component component) {
        if (BOTH.equals(s)) {
            lines.add(new Line(component, false));
        } else if (BOTHDYNAMIC.equals(s)) {
            Line l = new Line(component, false);
            lines.add(l);
            l.setDynamic(true);
        } else if (BOTHINDENT.equals(s)) {
            lines.add(new Line(component, true));
        } else if (LABEL.equals(s)) {
            if (lines.size() > 0 && lines.get(lines.size() - 1).isLabelFree())
                lines.get(lines.size() - 1).setLabel(component);
            else
                lines.add(new Line(component, LABEL));
        } else if (INPUT.equals(s)) {
            addInput(component);
        } else
            throw new IllegalArgumentException();
    }

    private Line addInput(Component component) {
        Line l;
        if (lines.size() > 0 && lines.get(lines.size() - 1).isInputFree()) {
            l = lines.get(lines.size() - 1);
            l.setInput(component);
        } else
            lines.add(l = new Line(component, INPUT));

        return l;
    }

    @Override
    public void removeLayoutComponent(Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dimension preferredLayoutSize(Container container) {
        return minimumLayoutSize(container);
    }

    private void initialize() {
        if (!initialized) {
            dynCount = 0;
            for (Line l : lines) {
                l.initialize();
                if (l.isDynamic()) dynCount++;
            }
            initialized = true;
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container container) {
        initialize();

        int both = 0;
        label = 0;
        int input = 0;
        int height = 0;
        for (Line l : lines) {
            if (l.isBoth()) {
                int w = l.getLabel().getPreferredSize().width;
                if (l.isIndent()) w += BOTHOFFSET;
                if (w > both) both = w;
            } else {
                if (l.getLabel() != null) {
                    int w = l.getLabel().getPreferredSize().width;
                    if (w > label) label = w;
                }
                if (l.getInput() != null) {
                    int w = l.getInput().getPreferredSize().width;
                    if (w > input) input = w;
                }
            }
            height += l.getHeight();
        }
        Insets in = container.getInsets();
        return new Dimension(Math.max(both + SPACEX * 2, label + input + SPACEX * 3) + in.left + in.right, height + in.top + in.bottom + (lines.size() + 1) * SPACEY);
    }

    @Override
    public void layoutContainer(Container container) {
        Dimension minSize = minimumLayoutSize(container);
        Insets in = container.getInsets();

        int dynAddendum = 0;
        if (dynCount > 0) dynAddendum = (container.getSize().height - minSize.height) / dynCount;


        int width = container.getSize().width;

        int top = in.top + SPACEY;
        for (Line l : lines) {
            int height = l.getHeight();
            if (l.isDynamic()) height += dynAddendum;
            if (l.isBoth()) {
                Component c = l.getLabel();
                if (l.isIndent()) {
                    c.setLocation(in.left + BOTHOFFSET + SPACEX, top);
                    c.setSize(width - BOTHOFFSET - SPACEX * 2 - in.left - in.right, height);
                } else {
                    c.setLocation(in.left + SPACEX, top);
                    c.setSize(width - SPACEX * 2 - in.left - in.right, height);
                }
            } else {
                Component labelComp = l.getLabel();
                if (labelComp != null) {
                    labelComp.setLocation(in.left + SPACEX, top);
                    labelComp.setSize(label, height);
                }
                Component inputComp = l.getInput();
                if (inputComp != null) {
                    inputComp.setLocation(label + in.left + SPACEX * 2, top);
                    inputComp.setSize(width - label - SPACEX * 3 - in.left - in.right, height);
                }
            }

            top += height + SPACEY;
        }
    }
}
