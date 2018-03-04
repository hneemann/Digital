/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */

/**
 * In this package you can find all the classes necessary to run the model.
 * So all the logic gates, memory and so on are implemented here.
 * This core model does not contain any information about its graphical representation.
 * But every single value is represented by the class {@link de.neemann.digital.core.ObservableValue}
 * and the graphical representation registers listeners tho the values to be notified if a state
 * change occurred, and maybe a repaint is necessary.
 */
package de.neemann.digital.core;
