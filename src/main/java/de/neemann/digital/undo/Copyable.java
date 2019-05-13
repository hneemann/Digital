/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.undo;

/**
 * Something which is editable and supports a history including undo and redo
 *
 * @param <A> the concrete type
 */
public interface Copyable<A extends Copyable<A>> {
    /**
     * @return a copy of itself
     */
    A createDeepCopy();
}
