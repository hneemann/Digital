/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */

/**
 * Classes to allow a simple synchronisation of model access.
 * The problem is, that every modification of a {@link de.neemann.digital.core.ObservableValue}
 * is a model access and needs to be synchronized. Synchronisation is necessary only if
 * the runtime clock is running activated. If the runtime clock does not run, all modifications
 * on the model are done by the GUI thread.
 */
package de.neemann.digital.gui.sync;
