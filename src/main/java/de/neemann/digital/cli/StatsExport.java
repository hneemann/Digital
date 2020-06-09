/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.stats.Statistics;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.lang.Lang;

import java.io.*;

/**
 * CLI stats exporter
 */
public class StatsExport extends SimpleCommand {
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
            File file = new File(digFile.get());
            ElementLibrary library = new ElementLibrary();
            library.setRootFilePath(file.getParentFile());
            ShapeFactory shapeFactory = new ShapeFactory(library, false);
            Circuit circuit = Circuit.loadCircuit(file, shapeFactory);

            Model model = new ModelCreator(circuit, library).createModel(false);
            Statistics stats = new Statistics(model);

            String outName;
            if (csvFile.isSet())
                outName = csvFile.get();
            else
                outName = digFile.get() + ".csv";

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outName)));
            new CSVWriter(stats.getTableModel()).writeTo(writer);

        } catch (IOException | ElementNotFoundException | PinException | NodeException e) {
            throw new CLIException(Lang.get("cli_errorCreatingStats"), e);
        }
    }
}
