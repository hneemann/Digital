/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.undo;

/**
 * Abstraction of a modification done on a object.
 * Make sure that EVERY modification is done via this interface!
 *
 * @param <A> the type of the object to modify
 */
public interface Modification<A> {

    /**
     * Modifies the object
     *
     * @param a the object to modify
     * @throws ModifyException ModifyException
     */
    void modify(A a) throws ModifyException;
}
