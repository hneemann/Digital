/*
 * Copyright (c) 2024 Ron Ren.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.extern.handler.ProcessInterface;
import de.neemann.digital.core.extern.handler.SocketInterface;

import java.io.File;
import java.io.IOException;

/**
 * application with shared mem map file comm
 */
public class ApplicationSocket implements Application {
    @Override
    public ProcessInterface start(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) throws IOException {
        String ipPort = "127.0.0.1:8009";
        if (code.length() > 0 && code.indexOf(":") > 0) {
            ipPort = code;
        }
        return new SocketInterface(ipPort, inputs, outputs);
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
