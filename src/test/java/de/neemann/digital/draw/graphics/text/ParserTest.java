package de.neemann.digital.draw.graphics.text;

import junit.framework.TestCase;

public class ParserTest extends TestCase {

    public void testSimple() throws ParseException {
        assertEquals("Decorate{Q_{i}, OVERLINE}", new Parser("~{Q_{i}}").parse().toString());
        assertEquals("Decorate{Q, OVERLINE}_{i}", new Parser("~Q_{i}").parse().toString());
        assertEquals("Decorate{Q, OVERLINE}", new Parser("~Q").parse().toString());
        assertEquals("Decorate{Q_{i}, MATH}", new Parser("$Q_i$").parse().toString());
        assertEquals("grüne Blätter", new Parser("grüne Blätter").parse().toString());
        assertEquals("hello Q^{i} world", new Parser("hello Q^i world").parse().toString());
        assertEquals("hello world", new Parser("hello   world").parse().toString());
        assertEquals("Q^{i}_{j}", new Parser("Q_j^i").parse().toString());
        assertEquals("Q^{i}_{j}", new Parser("Q^i_j").parse().toString());
        assertEquals("Q^{i}", new Parser("Q^i").parse().toString());
        assertEquals("Q_{i}", new Parser("Q_i").parse().toString());
        assertEquals("hello world", new Parser("hello world").parse().toString());
        assertEquals("Q", new Parser("Q").parse().toString());
        assertEquals("≥1", new Parser("≥1").parse().toString());

        assertEquals("Decorate{≥1, MATH}", new Parser("$≥1$").parse().toString());
    }

    public void testEnforceMath() throws ParseException {
        assertEquals("Decorate{Decorate{Q, OVERLINE}, MATH}", new Parser("$~Q$").parse().enforceMath().toString());
        assertEquals("Decorate{Decorate{Q, OVERLINE}, MATH}", new Parser("~Q").parse().enforceMath().toString());
        assertEquals("Decorate{Q_{i}, MATH}", new Parser("Q_i").parse().enforceMath().toString());
        assertEquals("hello Decorate{Q_{i}, MATH} world", new Parser("hello Q_i world").parse().enforceMath().toString());
        assertEquals("hello Decorate{Q_{i}, MATH} world", new Parser("hello Q_i world").parse().enforceMath().enforceMath().toString());
    }
}