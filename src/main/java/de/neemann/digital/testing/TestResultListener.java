/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.testing.parser.LineListener;

/**
 * Used to create the test result
 */
public interface TestResultListener extends LineListener {
    /**
     * Is called by the test executor to add the clock rows to the result
     *
     * @param description the description
     */
    void addClockRow(String description);
}
