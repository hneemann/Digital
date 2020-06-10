/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */

import de.neemann.digital.cli.Main;

/**
 * Entry point for the CLI interface.
 * Used to allow a more compact command line.
 * All work is delegated to {@link Main}.
 */
public final class CLI {

    private CLI() {
    }

    /**
     * Entry point for the CLI interface
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Main().main(args);
    }
}
