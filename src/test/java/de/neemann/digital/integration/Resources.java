package de.neemann.digital.integration;

import java.io.File;

/**
 * @author hneemann
 */
public class Resources {

    private static final class InstanceHolder {
        static final File FILE = createFile();
        private static final String PATH = "/home/hneemann/Dokumente/Java/digital/src/test/resources/";

        private static File createFile() {
            String testdata = System.getProperty("testdata");
            if (testdata == null) {
                System.out.println("environment variable testdata not set!!!");
                System.out.println("Try to use hardcoded " + PATH);
                testdata = PATH;

            }
            return new File(testdata);
        }

    }

    public static File getRoot() {
        return InstanceHolder.FILE;
    }

}
