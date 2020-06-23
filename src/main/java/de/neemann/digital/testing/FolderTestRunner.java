/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.parser.ParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Runs all tests in al circuits in a folder
 */
public class FolderTestRunner {
    private final ArrayList<FileToTest> files;
    private Thread thread;

    /**
     * Creates a new instance
     *
     * @param folder the folder to scan
     */
    public FolderTestRunner(File folder) {
        files = new ArrayList<>();
        scan(folder.getPath().length() + 1, folder);
    }

    private void scan(int rootLength, File folder) {
        File[] fileList = folder.listFiles();
        if (fileList != null) {
            Arrays.sort(fileList, Comparator.comparing(f -> f.getPath().toLowerCase()));
            for (File f : fileList)
                if (f.isDirectory())
                    scan(rootLength, f);
                else if (f.isFile() && f.getName().endsWith(".dig"))
                    files.add(new FileToTest(rootLength, f));
        }
    }

    /**
     * Starts all the tests.
     * The test execution is done in a new thread, so this method returns immediately.
     *
     * @param fileChangedListener the listsener to notify if a file status changed
     * @param shapeFactory        the shape factory
     * @param library             the element library
     */
    public void startTests(FileChangedListener fileChangedListener, ShapeFactory shapeFactory, ElementLibrary library) {
        thread = new Thread(new TestRunner(files, fileChangedListener, shapeFactory, library));
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Waits until tests are done.
     *
     * @throws InterruptedException InterruptedException
     */
    public void waitUntilFinished() throws InterruptedException {
        thread.join();
    }

    /**
     * @return the list of files to test
     */
    public ArrayList<FileToTest> getFiles() {
        return files;
    }

    /**
     * Describes the file to test
     */
    public static final class FileToTest {

        /**
         * the status of the file
         */
        public enum Status {
            /**
             * status unknown
             */
            unknown,
            /**
             * all tests have passed
             */
            passed,
            /**
             * there was an exception during model building or execution
             */
            error,
            /**
             * at least one test has failed
             */
            failed
        }

        private final File file;
        private final String name;
        private String message = "-";
        private FileToTest.Status status = FileToTest.Status.unknown;
        private int rowCount;


        private FileToTest(int rootLength, File file) {
            this.file = file;
            name = file.getPath().substring(rootLength);
        }

        /**
         * @return the name of this file
         */
        public String getName() {
            return name;
        }

        private void setMessage(String message, FileToTest.Status status) {
            this.message = message;
            this.status = status;
        }

        /**
         * @return the message to show
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return the status of this file
         */
        public Status getStatus() {
            return status;
        }

        /**
         * @return the tested file
         */
        public File getFile() {
            return file;
        }

        private void setTestRows(int rowCount) {
            this.rowCount = rowCount;
        }

        /**
         * @return the number of test case rows
         */
        public int getRowCount() {
            return rowCount;
        }
    }

    private static final class TestRunner implements Runnable {
        private final ArrayList<FileToTest> files;
        private final FileChangedListener fileChangedListener;
        private final ShapeFactory shapeFactory;
        private final ElementLibrary library;

        private TestRunner(ArrayList<FileToTest> files, FileChangedListener fileChangedListener, ShapeFactory shapeFactory, ElementLibrary library) {
            this.files = files;
            this.fileChangedListener = fileChangedListener;
            this.shapeFactory = shapeFactory;
            this.library = library;
        }

        @Override
        public void run() {
            for (int i = 0; i < files.size(); i++) {
                FileToTest f = files.get(i);
                try {
                    Circuit circuit = Circuit.loadCircuit(f.file, shapeFactory);
                    ArrayList<TestCase> testCases = new ArrayList<>();
                    for (VisualElement el : circuit.getTestCases()) {
                        String label = el.getElementAttributes().getLabel();
                        TestCaseDescription testData = el.getElementAttributes().get(TestCaseElement.TESTDATA);
                        testCases.add(new TestCase(label, testData));
                    }
                    if (testCases.isEmpty()) {
                        // if no test data is available, at least check if the model is error free
                        try {
                            new ModelCreator(circuit, library).createModel(false);
                            // if error free, issue a no test date message
                            setMessage(f, i, Lang.get("err_noTestData"), FileToTest.Status.unknown);
                        } catch (Exception e) {
                            setMessage(f, i, Lang.get("msg_errorCreatingModel"), FileToTest.Status.error);
                        }
                    } else {
                        StringBuilder sb = new StringBuilder();
                        int rowCount = 0;
                        for (TestCase tc : testCases) {
                            Model model = new ModelCreator(circuit, library).createModel(false);
                            try {
                                TestExecutor te = new TestExecutor(tc.testData).create(model);
                                if (te.allPassed()) {
                                    rowCount += te.getResult().getRows();
                                } else {
                                    if (sb.length() > 0)
                                        sb.append("; ");
                                    sb.append(Lang.get("msg_test_N_Failed", tc.label));
                                }
                            } catch (TestingDataException | NodeException e) {
                                if (sb.length() > 0)
                                    sb.append("; ");
                                sb.append(tc.label).append(": ").append(e.getMessage());
                            }
                        }
                        if (sb.length() == 0) {
                            f.setTestRows(rowCount);
                            setMessage(f, i, Lang.get("msg_testPassed_N", rowCount), FileToTest.Status.passed);
                        } else
                            setMessage(f, i, sb.toString(), FileToTest.Status.failed);
                    }

                } catch (IOException | NodeException | ElementNotFoundException | PinException | ParserException | RuntimeException e) {
                    setMessage(f, i, e.getMessage(), FileToTest.Status.error);
                }
            }
        }

        private void setMessage(FileToTest f, int i, String message, FileToTest.Status status) {
            f.setMessage(message, status);
            fileChangedListener.messageChanged(f, i);
        }
    }

    private static final class TestCase {
        private final String label;
        private final TestCaseDescription testData;

        private TestCase(String label, TestCaseDescription testData) {
            this.label = label;
            this.testData = testData;
        }
    }

    /**
     * Interface to notify a listener for changes
     */
    public interface FileChangedListener {
        /**
         * Called if a file message has changed
         *
         * @param f   the file changed
         * @param row the row index
         */
        void messageChanged(FileToTest f, int row);
    }
}
