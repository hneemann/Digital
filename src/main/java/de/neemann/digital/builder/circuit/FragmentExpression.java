/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.circuit;

import de.neemann.digital.core.io.Const;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * Fragment which handles an expression.
 */
public class FragmentExpression implements Fragment {

    private final ArrayList<FragmentHolder> fragments;
    private final Fragment merger;
    private Vector pos;
    private boolean normalLayout;
    private int alignedWidth;

    private static ArrayList<Fragment> createList(Fragment fragment) {
        ArrayList<Fragment> f = new ArrayList<>();
        f.add(fragment);
        return f;
    }

    /**
     * Creates a new instance
     *
     * @param fragment a single frgment as an input
     * @param merger   the merger
     */
    public FragmentExpression(Fragment fragment, Fragment merger) {
        this(createList(fragment), merger);
    }

    /**
     * The list of fragments is merged by a merger to a single output
     *
     * @param frags  the fragments to merge
     * @param merger the merger
     */
    public FragmentExpression(List<Fragment> frags, Fragment merger) {
        this.merger = merger;
        fragments = new ArrayList<>();
        for (Fragment fr : frags)
            fragments.add(new FragmentHolder(fr));
    }

    private Box doLayoutNormal() {
        ArrayList<ConstExpression> constExpr = new ArrayList<>();
        int height = 0;
        int width = 0;
        for (FragmentHolder fr : fragments) {
            fr.fragment.setPos(new Vector(0, height));
            if (isConst(fr.fragment))
                constExpr.add(new ConstExpression(fr.fragment, height));
            fr.box = fr.fragment.doLayout();

            height += fr.box.getHeight();
            int w = fr.box.getWidth();
            if (w > width)
                width = w;

            height += SIZE * 2;
        }
        height -= SIZE * 2;

        Box mergerBox = merger.doLayout();

        width += (fragments.size() / 2 + 1) * SIZE;

        if (alignedWidth > 0) {
            width = alignedWidth - mergerBox.getWidth();
        }

        for (ConstExpression ce : constExpr)
            ce.setXPos(width);

        int centerIndex = fragments.size() / 2;
        int y;
        if ((fragments.size() & 1) == 0) {
            // even number of inputs
            int y1 = fragments.get(centerIndex - 1).fragment.getOutputs().get(0).y;
            int y2 = fragments.get(centerIndex).fragment.getOutputs().get(0).y;
            y = raster((y1 + y2) / 2) - centerIndex * SIZE;
        } else {
            // odd number of inputs
            y = fragments.get(centerIndex).fragment.getOutputs().get(0).y - centerIndex * SIZE;
        }
        merger.setPos(new Vector(width, y));

        width += mergerBox.getWidth();

        return new Box(width, Math.max(height, y + mergerBox.getHeight()));
    }

    private boolean isConst(Fragment fragment) {
        return fragment instanceof FragmentVisualElement
                && ((FragmentVisualElement) fragment).getVisualElement().equalsDescription(Const.DESCRIPTION);
    }

    private Box doLayoutOnlyVariables() {

        int xPos = SIZE;
        if (alignedWidth > 0) {
            Box mergerBox = merger.doLayout();
            xPos = alignedWidth - mergerBox.getWidth();
        }


        Box mergerBox = merger.doLayout();
        merger.setPos(new Vector(xPos, 0));

        Iterator<Vector> in = merger.getInputs().iterator();
        for (FragmentHolder fr : fragments) {
            fr.fragment.setPos(new Vector(0, in.next().y));
            fr.box = fr.fragment.doLayout();
        }

        return new Box(mergerBox.getWidth() + SIZE, mergerBox.getHeight());
    }

    @Override
    public Box doLayout() {
        for (FragmentHolder fr : fragments)
            if (!(fr.fragment instanceof FragmentVariable)) {
                normalLayout = true;
                return doLayoutNormal();
            }

        normalLayout = false;
        return doLayoutOnlyVariables();
    }

    private int raster(int k) {
        return (int) Math.round((double) k / SIZE) * SIZE;
    }

    @Override
    public void setPos(Vector pos) {
        this.pos = pos;
    }

    @Override
    public void addToCircuit(Vector offset, Circuit circuit) {
        Vector p = pos.add(offset);
        merger.addToCircuit(p, circuit);
        for (int i = 0; i < fragments.size(); i++) {
            FragmentHolder fr = fragments.get(i);
            fr.fragment.addToCircuit(p, circuit);

            Vector pin = fr.fragment.getOutputs().get(0);

            Vector start = pin.add(p);
            Vector end = merger.getInputs().get(i).add(p);

            int back = 0;
            if (normalLayout)
                back = calcBackOffset(fragments.size(), i);

            if (back > 0) {
                Vector inter2 = end.add(-back * SIZE, 0);
                Vector inter1 = new Vector(inter2.x, start.y);
                circuit.add(new Wire(start, inter1));
                circuit.add(new Wire(inter1, inter2));
                circuit.add(new Wire(inter2, end));
            } else {
                circuit.add(new Wire(start, end));
            }
        }
    }

    static int calcBackOffset(int size, int i) {
        if ((size & 1) != 0 && i == (size - 1) / 2)
            return 0;

        if (i >= size / 2)
            return size - i;
        else
            return i + 1;
    }

    @Override
    public List<Vector> getInputs() {
        ArrayList<Vector> pins = new ArrayList<>();
        for (FragmentHolder fr : fragments)
            pins.addAll(Vector.add(fr.fragment.getInputs(), pos));

        return pins;
    }

    @Override
    public List<Vector> getOutputs() {
        return Vector.add(merger.getOutputs(), pos);
    }

    /**
     * Sets the alignment width.
     * If set the fragment will be layout to the given width
     *
     * @param width the required width
     */
    public void setWidth(int width) {
        this.alignedWidth = width;
    }

    private static final class FragmentHolder {
        private final Fragment fragment;
        private Box box;

        FragmentHolder(Fragment fragment) {
            this.fragment = fragment;
        }
    }

    private static final class ConstExpression {

        private final Fragment fragment;
        private final int height;

        ConstExpression(Fragment fragment, int height) {
            this.fragment = fragment;
            this.height = height;
        }

        void setXPos(int xPos) {
            fragment.setPos(new Vector(xPos - SIZE, height));
        }
    }

    @Override
    public <V extends FragmentVisitor> V traverse(V v) {
        v.visit(this);
        for (FragmentHolder f : fragments)
            f.fragment.traverse(v);
        merger.traverse(v);
        return v;
    }
}
