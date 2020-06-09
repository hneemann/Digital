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
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
            File file = new File(digFile.get());
            ElementLibrary library = new ElementLibrary();
            library.setRootFilePath(file.getParentFile());
            ShapeFactory shapeFactory = new ShapeFactory(library, ieeeShapes.get());
            Circuit circuit = Circuit.loadCircuit(file, shapeFactory);

            String outName;
            if (svgFile.isSet())
                outName = svgFile.get();
            else
                outName = digFile.get() + ".svg";

            OutputStream out = new FileOutputStream(outName);
            new Export(circuit, o -> new GraphicSVG(o, attr)).export(out);
        } catch (IOException e) {
            throw new CLIException(Lang.get("cli_errorCreatingSVG"), e);
        }
    }
}
