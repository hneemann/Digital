/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.text.text;

import de.neemann.digital.draw.graphics.text.ParseException;

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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Text t : list)
            sb.append(t.toString());
        return sb.toString();
    }

    /**
     * @return the last list element which is used to add an index
     * @throws ParseException ParseException
     */
    public Index getIndex() throws ParseException {
        if (list.isEmpty())
            throw new ParseException("no previous text element");
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

    /**
     * @return the size of the sentence
     */
    public int size() {
        return list.size();
    }
}
