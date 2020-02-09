/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import junit.framework.TestCase;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TestExampleNamesUnique extends TestCase {

    public void testExamples() throws Exception {
        File basedir = new File(Resources.getRoot(), "../../../");
        File sourceFilename = new File(basedir, "distribution/Assembly.xml");

        HashMap<String, File> names = new HashMap<>();
        Element assembly = new SAXBuilder().build(sourceFilename).getRootElement();
        for (Element fs : assembly.getChild("fileSets", null).getChildren("fileSet", null)) {
            String outDir = fs.getChild("outputDirectory", null).getText();
            if (outDir.startsWith("/examples/")) {
                String srcDir = fs.getChild("directory", null).getText();
                srcDir = srcDir.replace("${basedir}", basedir.getPath());
                new FileScanner(f -> {
                    String name = f.getName();
                    File present = names.get(name);
                    if (present != null) {
                        throw new IOException("name not unique\n" + present.getPath() + "\n" + f.getPath());
                    }
                    names.put(name, f);
                }).noOutput().scan(new File(srcDir));
            }
        }
    }

}
