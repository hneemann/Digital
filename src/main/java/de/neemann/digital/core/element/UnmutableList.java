package de.neemann.digital.core.element;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A simple unmutable array
 *
 * @param <T> the items type
 * @author hneemann
 */
public class UnmutableList<T> implements Iterable<T> {
    private final T[] items;

    /**
     * Combines two lists
     *
     * @param a   first list
     * @param b   second list
     * @param <T> the Type of the elements
     * @return the new combined list
     */
    public static <T> UnmutableList<T> combine(UnmutableList<T> a, UnmutableList<T> b) {
        T[] com = Arrays.copyOf(a.items, a.size() + b.size());
        System.arraycopy(b.items, 0, com, a.size(), b.size());
        return new UnmutableList<>(com);
    }

    /**
     * Creates a new instance
     *
     * @param items items
     */
    public UnmutableList(T... items) {
        this.items = items;
    }

    /**
     * Creates a new instance
     *
     * @param items items
     */
    public <U extends T> UnmutableList(UnmutableList<U> items) {
        this.items = items.items;
    }

    /**
     * Creates a partial list
     *
     * @param items the original list
     * @param from  inclusive
     * @param to    exclusive
     * @param <U>   type of items
     */
    public <U extends T> UnmutableList(UnmutableList<U> items, int from, int to) {
        this.items = Arrays.copyOfRange(items.items, from, to);
    }

    /**
     * @return the size
     */
    public int size() {
        return items.length;
    }

    /**
     * Return the item with given index
     *
     * @param i index
     * @return the item
     */
    public T get(int i) {
        return items[i];
    }

    @Override
    public Iterator<T> iterator() {
        return Arrays.asList(items).iterator();
    }

    /**
     * @return this array as a list
     */
    public List<T> asCollection() {
        return Collections.unmodifiableList(Arrays.asList(items));
    }
}
