/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.ArgumentKey;
import de.neemann.digital.cli.cli.BasicCommand;
import de.neemann.digital.cli.cli.CLIException;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Export;
import de.neemann.digital.draw.graphics.GraphicSVG;
import de.neemann.digital.draw.graphics.SVGSettings;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.IOException;

/**
 * CLI svg exporter
 */
public class SVGExport extends BasicCommand {
    private final ElementAttributes attr;
    private final Argument<String> digFile;
    private final Argument<String> svgFile;
    private final Argument<Boolean> ieeeShapes;

    /**
     * Creates the SVG export command
     */
    public SVGExport() {
        super("svg");

        digFile = addArgument(new Argument<>("dig", "", false));
        svgFile = addArgument(new Argument<>("svg", "", true));
        ieeeShapes = addArgument(new Argument<>("ieee", false, true));

        attr = new ElementAttributes();
        for (Key<?> k : SVGSettings.createKeyList())
            addArgument(new ArgumentKey<>(k, attr, 4));
    }

    @Override
    protected void execute() throws CLIException {
        try {
            Circuit circuit = new CircuitLoader(digFile.get(), ieeeShapes.get()).getCircuit();

            String outName;
            if (svgFile.isSet())
                outName = svgFile.get();
            else
                outName = digFile.get() + ".svg";

            new Export(circuit, o -> new GraphicSVG(o, attr)).export(new File(outName));
        } catch (IOException e) {
            throw new CLIException(Lang.get("cli_errorCreatingSVG"), e);
        }
    }
}
