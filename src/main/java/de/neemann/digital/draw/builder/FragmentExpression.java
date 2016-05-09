package de.neemann.digital.draw.builder;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * @author hneemann
 */
public class FragmentExpression implements Fragment {

    private final ArrayList<FragmentHolder> fragments;
    private final Fragment merger;
    private Vector pos;

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
    public FragmentExpression(ArrayList<Fragment> frags, Fragment merger) {
        this.merger = merger;
        fragments = new ArrayList<>();
        for (Fragment fr : frags)
            fragments.add(new FragmentHolder(fr));
    }

    private Box doLayoutNormal() {
        int height = 0;
        int width = 0;
        for (FragmentHolder fr : fragments) {
            fr.fragment.setPos(new Vector(0, height));
            fr.box = fr.fragment.doLayout();

            height += fr.box.getHeight();
            int w = fr.box.getWidth();
            if (w > width)
                width = w;

            height += SIZE * 2;
        }
        height -= SIZE * 2;

        Box mergerBox = merger.doLayout();

        if (isLong())
            if (fragments.size() > 3)
                width += SIZE * 3;
            else
                width += SIZE * 2;
        else
            width += SIZE;

        if ((fragments.size() & 1) == 0) {
            // even number of inputs
            merger.setPos(new Vector(width, raster((height - mergerBox.getHeight()) / 2)));
        } else {
            // odd number of inputs
            int centerIndex = fragments.size() / 2;
            int y = fragments.get(centerIndex).fragment.getOutputs().get(0).y - centerIndex * SIZE;
            merger.setPos(new Vector(width, y));
        }

        width += mergerBox.getWidth();

        return new Box(width, Math.max(height, mergerBox.getHeight()));
    }

    private Box doLayoutOnlyVariables() {
        Box mergerBox = merger.doLayout();
        merger.setPos(new Vector(SIZE * 2, 0));

        Iterator<Vector> in = merger.getInputs().iterator();
        for (FragmentHolder fr : fragments) {
            fr.fragment.setPos(new Vector(0, in.next().y));
            fr.box = fr.fragment.doLayout();
        }

        return new Box(mergerBox.getWidth() + SIZE * 2, mergerBox.getHeight());
    }

    @Override
    public Box doLayout() {
        for (FragmentHolder fr : fragments)
            if (!(fr.fragment instanceof FragmentVariable))
                return doLayoutNormal();

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
        Iterator<Vector> inputs = merger.getInputs().iterator();
        for (FragmentHolder fr : fragments) {
            fr.fragment.addToCircuit(p, circuit);

            Vector pin = fr.fragment.getOutputs().get(0);

            Vector start = pin.add(p);
            Vector end = inputs.next().add(p);
            if (isLong()) {
                int dx = end.x - start.x - SIZE;
                if (fragments.size() > 3)
                    dx -= SIZE;

                Vector inter1 = start.add(dx, 0);
                Vector inter2 = end.add(-SIZE, 0);
                circuit.add(new Wire(start, inter1));
                circuit.add(new Wire(inter1, inter2));
                circuit.add(new Wire(inter2, end));
            } else {
                circuit.add(new Wire(start, end));
            }

            p.add(0, fr.box.getHeight() + SIZE);
        }
    }

    @Override
    public List<Vector> getInputs() {
        ArrayList<Vector> pins = new ArrayList<>();
        Vector p = new Vector(pos);
        for (FragmentHolder fr : fragments) {
            pins.addAll(Vector.add(fr.fragment.getInputs(), p));
            p.add(0, fr.box.getHeight() + SIZE);
        }
        return pins;
    }

    @Override
    public List<Vector> getOutputs() {
        return Vector.add(merger.getOutputs(), pos);
    }

    private boolean isLong() {
        return fragments.size() > 1;
    }

    private class FragmentHolder {
        private final Fragment fragment;
        private Box box;

        FragmentHolder(Fragment fragment) {
            this.fragment = fragment;
        }
    }
}
