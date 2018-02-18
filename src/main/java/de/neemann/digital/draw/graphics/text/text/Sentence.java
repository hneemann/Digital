package de.neemann.digital.draw.graphics.text.text;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A sentence.
 * A list of text fragments
 */
public class Sentence implements Text, Iterable<Text> {

    private final ArrayList<Text> list;

    /**
     * Creates a new instance
     */
    public Sentence() {
        list = new ArrayList<>();
    }

    /**
     * Adds a text fragment
     *
     * @param text the text fragment to add
     * @return this for chained calls
     */
    public Sentence add(Text text) {
        if (text != null)
            list.add(text);
        return this;
    }

    @Override
    public Text simplify() {
        if (list.size() == 1)
            return list.get(0);
        else
            return this;
    }

    @Override
    public Text enforceMath() {
        list.replaceAll(Text::enforceMath);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Text t : list)
            sb.append(t.toString());
        return sb.toString();
    }

    /**
     * @return the last list element which is used to add an index
     */
    public Index getIndex() {
        Text last = list.get(list.size() - 1);
        if (last instanceof Index) {
            return (Index) last;
        } else {
            Index index = new Index(last);
            list.set(list.size() - 1, index);
            return index;
        }
    }

    @Override
    public Iterator<Text> iterator() {
        return list.iterator();
    }

    /**
     * @return true if last list entry is a blank
     */
    public boolean lastIsBlank() {
        if (list.isEmpty())
            return true;
        else
            return list.get(list.size() - 1) instanceof Blank;
    }
}
