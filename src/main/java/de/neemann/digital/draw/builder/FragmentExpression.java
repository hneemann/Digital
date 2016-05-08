package de.neemann.digital.draw.builder;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;

import java.util.ArrayList;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * @author hneemann
 */
public class FragmentExpression implements Fragment {

    private final ArrayList<FragmentHolder> fragments;
    private final Fragment merger;
    private Vector pos;

    public FragmentExpression(ArrayList<Fragment> frags, Fragment merger) {
        this.merger = merger;
        fragments = new ArrayList<>();
        for (Fragment fr : frags)
            fragments.add(new FragmentHolder(fr));
    }

    @Override
    public Vector output() {
        return new Vector(0, 0);
    }

    @Override
    public Box doLayout() {
        int height = 0;
        int width = 0;
        for (FragmentHolder fr : fragments) {
            fr.fragment.setPos(new Vector(0, height));
            fr.box = fr.fragment.doLayout();

            height += fr.box.getHeight();
            int w = fr.box.getWidth();
            if (w > width)
                width = w;

            height += SIZE;
        }
        height -= SIZE;

        Box mergerBox = merger.doLayout();

        width += SIZE;

        merger.setPos(new Vector(width, (height - mergerBox.getHeight()) / 2));

        width += mergerBox.getWidth();

        return new Box(width, Math.max(height, mergerBox.getHeight()));
    }

    @Override
    public void setPos(Vector pos) {
        this.pos = pos;
    }

    @Override
    public void addToCircuit(Vector offset, Circuit circuit) {
        Vector p = pos.add(offset);
        merger.addToCircuit(p, circuit);
        for (FragmentHolder fr : fragments) {
            fr.fragment.addToCircuit(p, circuit);
            p.add(0, fr.box.getHeight() + SIZE);
        }
    }

    private class FragmentHolder {
        private final Fragment fragment;
        private Box box;

        FragmentHolder(Fragment fragment) {
            this.fragment = fragment;
        }
    }
}
