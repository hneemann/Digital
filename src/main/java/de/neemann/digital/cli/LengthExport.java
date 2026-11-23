/*
 * Copyright (c) 2025 Alessandro Pellegrini.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.analyse.AnalyseException;
import de.neemann.digital.analyse.ModelAnalyser;
import de.neemann.digital.analyse.PathLenAnalyser;
import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.BasicCommand;
import de.neemann.digital.cli.cli.CLIException;
import de.neemann.digital.core.BacktrackException;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.lang.Lang;

import java.io.IOException;

/**
 * CLI critical path exporter
 */
public class LengthExport extends BasicCommand {
    private final Argument<String> digFile;

    /**
     * Creates the critical path export command
     */
    public LengthExport() {
        super("length");
        digFile = addArgument(new Argument<>("dig", "", false));
    }

    @Override
    protected void execute() throws CLIException {
        try {
            Model model = new CircuitLoader(digFile.get()).createModel();
            ModelAnalyser modelAnalyser = new ModelAnalyser(model);
            PathLenAnalyser pathLenAnalyser = new PathLenAnalyser(modelAnalyser);
            System.out.println(pathLenAnalyser.getMaxPathLen());
        } catch (IOException | ElementNotFoundException | PinException | NodeException | BacktrackException
                 | AnalyseException e) {
            throw new CLIException(Lang.get("cli_errorComputingCriticalPath"), e);
        }
    }
}
