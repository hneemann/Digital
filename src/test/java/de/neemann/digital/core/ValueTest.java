/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import junit.framework.TestCase;

public class ValueTest extends TestCase {

    public void testSize() {
        assertEquals(3, new Value(-1, 2).getValue());
        assertEquals(1, new Value(1, 2).getValue());
        assertEquals(-1, new Value(-1, 2).getValueSigned());
        assertEquals(-1, new Value(3, 2).getValueSigned());
    }

}
