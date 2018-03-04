/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.element;

import java.util.*;

/**
 * A simple immutable array
 *
 * @param <T> the items type
 */
public class ImmutableList<T> implements List<T> {
    private final T[] items;

    /**
     * Combines two lists
     *
     * @param a   first list
     * @param b   second list
     * @param <T> the Type of the elements
     * @return the new combined list
     */
    public static <T> ImmutableList<T> combine(ImmutableList<T> a, ImmutableList<T> b) {
        T[] com = Arrays.copyOf(a.items, a.size() + b.size());
        System.arraycopy(b.items, 0, com, a.size(), b.size());
        return new ImmutableList<>(com);
    }

    /**
     * Creates a new instance
     *
     * @param items items
     */
    public ImmutableList(T... items) {
        this.items = items;
    }

    /**
     * Creates a new instance
     *
     * @param items items
     * @param <U>   the item type
     */
    public <U extends T> ImmutableList(ImmutableList<U> items) {
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
    public <U extends T> ImmutableList(ImmutableList<U> items, int from, int to) {
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
        return new Iterator<T>() {
            private int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < items.length;
            }

            @Override
            public T next() {
                if (pos >= items.length)
                    throw new IndexOutOfBoundsException();
                return items[pos++];
            }
        };
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(items, items.length);
    }

    // ***************************************************
    // Dummy list implementations
    // All are throwing an UnsupportedOperationException
    // ***************************************************

    @Override
    public <T1> T1[] toArray(T1[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
