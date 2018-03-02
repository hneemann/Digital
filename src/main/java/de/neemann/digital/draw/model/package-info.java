/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */

/**
 * The classes in this package are used to build the model from the used circuits.
 * If a model is build, all the included nested circuits are replicated as often as they are used in the
 * circuit.
 * It behaves different from a method call in a programming language. Its more like a macro expansion.
 * At the end the model only consists of the available primitives. The wiring is resolved and the connections to
 * nested circuits are resolved by replacing all the inputs and outputs by direct connections.
 */
package de.neemann.digital.draw.model;
