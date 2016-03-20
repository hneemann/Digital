package de.neemann.digital.core.wiring;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class SplitterTestMix extends TestCase {

    public void test1() throws Exception {
        ObservableValue a = new ObservableValue("a", 8);
        ObservableValue b = new ObservableValue("b", 8);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(AttributeKey.InputSplit, "8,8")
                .set(AttributeKey.OutputSplit, "4,12"));

        splitter.setInputs(a, b);
        assertEquals(2, a.observerCount());
        assertEquals(1, b.observerCount());

        ObservableValue[] outputs = splitter.getOutputs();
        assertEquals(2, outputs.length);

        TestExecuter sc = new TestExecuter().setInputs(a, b).setOutputsOf(splitter);
        sc.check(0x00, 0x00, 0x0, 0x000);

        sc.check(0x01, 0x00, 0x1, 0x000);
        sc.check(0x10, 0x00, 0x0, 0x001);
        sc.check(0x00, 0x01, 0x0, 0x010);
        sc.check(0x00, 0x10, 0x0, 0x100);

        sc.check(0x0f, 0x00, 0xf, 0x000);
        sc.check(0xf0, 0x00, 0x0, 0x00f);
        sc.check(0x00, 0x0f, 0x0, 0x0f0);
        sc.check(0x00, 0xf0, 0x0, 0xf00);

        sc.check(0xc0, 0xab, 0x0, 0xabc);
    }

    public void test2() throws Exception {
        ObservableValue a = new ObservableValue("a", 8);
        ObservableValue b = new ObservableValue("b", 8);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(AttributeKey.InputSplit, "8,8")
                .set(AttributeKey.OutputSplit, "12,4"));

        splitter.setInputs(a, b);
        assertEquals(1, a.observerCount());
        assertEquals(2, b.observerCount());

        ObservableValue[] outputs = splitter.getOutputs();
        assertEquals(2, outputs.length);

        TestExecuter sc = new TestExecuter().setInputs(a, b).setOutputsOf(splitter);
        sc.check(0x00, 0x00, 0x000, 0x0);

        sc.check(0x01, 0x00, 0x001, 0x0);
        sc.check(0x10, 0x00, 0x010, 0x0);
        sc.check(0x00, 0x01, 0x100, 0x0);
        sc.check(0x00, 0x10, 0x000, 0x1);

        sc.check(0x0f, 0x00, 0x00f, 0x0);
        sc.check(0xf0, 0x00, 0x0f0, 0x0);
        sc.check(0x00, 0x0f, 0xf00, 0x0);
        sc.check(0x00, 0xf0, 0x000, 0xf);

        sc.check(0xbc, 0xda, 0xabc, 0xd);
    }

}