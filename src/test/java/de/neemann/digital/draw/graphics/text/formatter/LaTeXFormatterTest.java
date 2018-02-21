package de.neemann.digital.draw.graphics.text.formatter;


import de.neemann.digital.draw.graphics.text.ParseException;
import de.neemann.digital.draw.graphics.text.Parser;
import junit.framework.TestCase;

public class LaTeXFormatterTest extends TestCase {

    public void testSimple() throws ParseException {
        assertEquals("Q", LaTeXFormatter.format(new Parser("Q").parse()));
        assertEquals("$Q$", LaTeXFormatter.format(new Parser("$Q$").parse()));
        assertEquals("$Q^i$", LaTeXFormatter.format(new Parser("Q^i").parse()));
        assertEquals("$Q^i$", LaTeXFormatter.format(new Parser("Q^{i}").parse()));
        assertEquals("$Q^{in}$", LaTeXFormatter.format(new Parser("Q^{in}").parse()));
        assertEquals("$\\overline{\\mbox{Q}}$", LaTeXFormatter.format(new Parser("~Q").parse()));
        assertEquals("$\\overline{Q}_i$", LaTeXFormatter.format(new Parser("~Q_i").parse()));
        assertEquals("$\\overline{Q_i}$", LaTeXFormatter.format(new Parser("~{Q_i}").parse()));
        assertEquals("Hello World", LaTeXFormatter.format(new Parser("Hello World").parse()));
        assertEquals("$\\geq\\!\\!{}$1", LaTeXFormatter.format(new Parser("≥1").parse()));
        assertEquals("a\\textless{}b", LaTeXFormatter.format(new Parser("a<b").parse()));
        assertEquals("$a<b$", LaTeXFormatter.format(new Parser("$a<b$").parse()));
        assertEquals("a\\textgreater{}b", LaTeXFormatter.format(new Parser("a>b").parse()));
        assertEquals("$a>b$", LaTeXFormatter.format(new Parser("$a>b$").parse()));
        assertEquals("a\\&b", LaTeXFormatter.format(new Parser("a&b").parse()));
        assertEquals("a$\\neg{}$b", LaTeXFormatter.format(new Parser("a¬b").parse()));
        assertEquals("$a\\neg{}b$", LaTeXFormatter.format(new Parser("$a¬b$").parse()));
    }
}