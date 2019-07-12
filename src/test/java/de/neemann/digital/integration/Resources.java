/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Helper to locate the resources
 */
public class Resources {

    private static final class InstanceHolder {
        private static final File FILE = createFile();

        private static File createFile() {
            String testdata = System.getProperty("testdata");
            if (testdata != null) {
                final File r = new File(testdata);
                if (r.exists())
                    return r;
            }

            try {
                String path = Resources.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replace('\\', '/');
                int p = path.lastIndexOf("/target/");
                if (p >= 0) {
                    File r = new File(path.substring(0, p) + "/src/test/resources/");
                    if (r.exists())
                        return r;
                }
            } catch (URISyntaxException e) {
                throw new Error("could not find the test data folder /src/test/resources/", e);
            }

            throw new Error("could not find the test data folder /src/test/resources/");
        }

    }

    public static File getRoot() {
        return InstanceHolder.FILE;
    }

}
