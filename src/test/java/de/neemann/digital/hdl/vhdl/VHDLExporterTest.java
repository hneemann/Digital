package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import junit.framework.TestCase;

import java.io.IOException;

public class VHDLExporterTest extends TestCase {

    public void testPortIn() throws IOException {
        Circuit c = new Circuit();
        c.add(new VisualElement(In.DESCRIPTION.getName()).setAttribute(Keys.LABEL, "A"));
        c.add(new VisualElement(In.DESCRIPTION.getName()).setAttribute(Keys.LABEL, "B"));

        assertEquals("  port (\n" +
                "    A: in std_logic;\n" +
                "    B: in std_logic\n" +
                "  );\n", new VHDLExporter(null).writePort(c).toString());
    }

    public void testPortInClockOut() throws IOException {
        Circuit c = new Circuit();
        c.add(new VisualElement(In.DESCRIPTION.getName()).setAttribute(Keys.LABEL, "A"));
        c.add(new VisualElement(Clock.DESCRIPTION.getName()).setAttribute(Keys.LABEL, "B"));
        c.add(new VisualElement(Out.DESCRIPTION.getName()).setAttribute(Keys.LABEL, "Y"));

        assertEquals("  port (\n" +
                "    A: in std_logic;\n" +
                "    B: in std_logic;\n" +
                "    Y: out std_logic\n" +
                "  );\n", new VHDLExporter(null).writePort(c).toString());
    }

    public void testInOut4Bit() throws IOException {
        Circuit c = new Circuit();
        c.add(new VisualElement(In.DESCRIPTION.getName()).setAttribute(Keys.LABEL, "A").setAttribute(Keys.BITS, 4));
        c.add(new VisualElement(Out.DESCRIPTION.getName()).setAttribute(Keys.LABEL, "Y").setAttribute(Keys.BITS, 4));

        assertEquals("  port (\n" +
                "    A: in std_logic_vector (3 downto 0);\n" +
                "    Y: out std_logic_vector (3 downto 0)\n" +
                "  );\n", new VHDLExporter(null).writePort(c).toString());
    }


}