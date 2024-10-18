/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.extern.handler.ProcessInterface;
import de.neemann.digital.core.extern.handler.SocketInterface;

import java.io.File;
import java.io.IOException;

/**
 * application with socket tcp/ip comm
 */
public class ApplicationSocket implements Application {
    @Override
    public ProcessInterface start(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) throws IOException {
        return new SocketInterface();
    }

    @Override
    public boolean checkSupported() {
        return true;
    }

    @Override
    public String checkCode(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) throws IOException {
        return null;
    }
}
