/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.extern.handler.ProcessInterface;
import de.neemann.digital.core.extern.handler.StdIOInterface;

import java.io.IOException;

/**
 * The given code is used as an application name and this application is started
 */
public class ApplicationGeneric implements Application {
    @Override
    public ProcessInterface start(String label, String code, PortDefinition inputs, PortDefinition outputs) throws IOException {
        String[] args = code.split(" ");
        ProcessBuilder pb = new ProcessBuilder(args).redirectErrorStream(true);
        return new StdIOInterface(pb.start());
    }
}
