/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import java.io.File;
import java.util.Set;

/**
 * Every exception that can provide an origin is implementing this interface.
 */
public interface ExceptionWithOriginInterface {

    /**
     * @return the origin of the error as a set of files
     */
    Set<File> getOrigin();
}
