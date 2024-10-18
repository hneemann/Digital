/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern.handler;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;

import java.io.IOException;

/**
 * socket set get
 */
public class SocketInterface implements ProcessInterface {
    @Override
    public void writeValues(ObservableValues values) throws IOException {
        for (ObservableValue v : values) {
            v.set(1, 0);
        }
    }

    @Override
    public void readValues(ObservableValues values) throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
