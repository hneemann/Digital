/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern.handler;

import java.io.IOException;

/**
 * Starts the string given in params as an external application
 */
public class Generic extends StdIOProcess {
    /**
     * Creates a new simple process
     *
     * @param file the name of the application to start
     * @throws IOException IOException
     */
    public Generic(String file) throws IOException {
        String[] args = file.split(" ");
        ProcessBuilder pb = new ProcessBuilder(args).redirectErrorStream(true);
        setProcess(pb.start());
    }
}
