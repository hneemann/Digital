/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.BasicCommand;
import de.neemann.digital.cli.cli.CLIException;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.stats.Statistics;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.lang.Lang;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * CLI stats exporter
 */
public class StatsExport extends BasicCommand {
    private final Argument<String> digFile;
    private final Argument<String> csvFile;

    /**
     * Creates the stats export command
     */
    public StatsExport() {
        super("stats");
        digFile = addArgument(new Argument<>("dig", "", false));
        csvFile = addArgument(new Argument<>("csv", "", true));
    }

    @Override
    protected void execute() throws CLIException {
        try {
            Model model = new CircuitLoader(digFile.get()).createModel();
            Statistics stats = new Statistics(model);

            BufferedWriter writer;
            if (csvFile.isSet())
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile.get())));
            else
                writer = new BufferedWriter(new OutputStreamWriter(System.out));

            new CSVWriter(stats.getTableModel()).writeTo(writer);

        } catch (IOException | ElementNotFoundException | PinException | NodeException e) {
            throw new CLIException(Lang.get("cli_errorCreatingStats"), e);
        }
    }
}
