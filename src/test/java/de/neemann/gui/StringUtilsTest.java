package de.neemann.gui;

import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hneemann on 29.10.16.
 */
public class StringUtilsTest extends TestCase {

    public void testBreakLines() throws Exception {
        assertEquals("this is a test string", StringUtils.breakLines("this \n\n is \n a   test \n\r    string", 60));
        assertEquals("this is a test\nstring", StringUtils.breakLines("this \n\n is \n a   test \n\r    string", 10));
        assertEquals("This is a test string. This\n" +
                "is a test string. This is a\n" +
                "test string.", StringUtils.breakLines("This is a test string. This is a test string. This is a test string.", 20));
    }

    public void testBreakLinesLabel() throws Exception {
        assertEquals("a) This is a test string. This\n" +
                "   is a test string. This is\n" +
                "   a test string.", StringUtils.breakLinesLabel("a)", 3, "This is a test string. This is a test string. This is a test string.", 22));
    }

}