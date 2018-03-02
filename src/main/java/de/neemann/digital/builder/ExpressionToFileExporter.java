/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder;

import de.neemann.digital.builder.jedec.FuseMapFillerException;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Used to create a output chain of files created
 */
public class ExpressionToFileExporter {

    private final ExpressionExporter exporter;
    private final ArrayList<PostProcess> postProcesses;

    /**
     * Creates a new instance
     *
     * @param exporter the initial export to create the initial file
     */
    public ExpressionToFileExporter(ExpressionExporter exporter) {
        this.exporter = exporter;
        postProcesses = new ArrayList<>();
    }

    /**
     * @return the initial exporter
     */
    public ExpressionExporter getExporter() {
        return exporter;
    }

    /**
     * Is delegated to exporter.getPinMap.
     *
     * @return the pin map
     */
    public PinMap getPinMapping() {
        return exporter.getPinMapping();
    }

    /**
     * Is delegated to exporter.getBuilder.
     *
     * @return the builder
     */
    public BuilderInterface getBuilder() {
        return exporter.getBuilder();
    }

    /**
     * Adds a processing step.
     * All steps are executed after the initial fals has been created.
     *
     * @param postProcess the process to start
     * @return this for chained calls
     */
    public ExpressionToFileExporter addProcessingStep(PostProcess postProcess) {
        postProcesses.add(postProcess);
        return this;
    }

    /**
     * Runs the export chain
     *
     * @param file the name of the initial file
     * @throws IOException            IOException
     * @throws PinMapException        PinMapException
     * @throws FuseMapFillerException FuseMapFillerException
     */
    public void export(File file) throws IOException, PinMapException, FuseMapFillerException {
        try (OutputStream out = new FileOutputStream(file)) {
            exporter.writeTo(out);
        }
        for (PostProcess p : postProcesses)
            try {
                file = p.execute(file);
            } catch (IOException e) {
                throw new IOException(Lang.get("err_postProcessErrorIn_N0", p.getName()), e);
            }
    }

    /**
     * PostProcess is used to start further steps creating the final output file
     */
    public interface PostProcess {
        /**
         * Execute a new process
         *
         * @param file the file to process
         * @return the new file created or, if no file is created the given file is returned
         * @throws IOException IOException
         */
        File execute(File file) throws IOException;

        /**
         * @return the name of this post processing step
         */
        String getName();
    }
}
