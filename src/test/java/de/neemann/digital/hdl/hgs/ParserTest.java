/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParserTest extends TestCase {

    public void testParseExpressionArith() throws IOException, ParserException, EvalException {
        assertEquals(3L, new Parser("1+2").parseExpression().value(new Context()));
        assertEquals("HelloWorld", new Parser("\"Hello\"+\"World\"").parseExpression().value(new Context()));
        assertEquals(3L, new Parser("5-2").parseExpression().value(new Context()));
        assertEquals(10L, new Parser("5*2").parseExpression().value(new Context()));
        assertEquals(2L, new Parser("6/3").parseExpression().value(new Context()));
        assertEquals(8L, new Parser("1<<3").parseExpression().value(new Context()));
        assertEquals(2L, new Parser("16>>3").parseExpression().value(new Context()));
        assertEquals(4L, new Parser("9%5").parseExpression().value(new Context()));

        assertEquals(-5L, new Parser("-5").parseExpression().value(new Context()));
        assertEquals(6L, new Parser("2*(1+2)").parseExpression().value(new Context()));

        try {
            new Parser("1+").parseExpression().value(new Context());
            fail();
        } catch (ParserException e) {
            assertTrue(true);
        }

        try {
            new Parser("1+\"Hallo\"").parseExpression().value(new Context());
            fail();
        } catch (EvalException e) {
            assertTrue(true);
        }

    }

    public void testParseExpressionCompare() throws IOException, ParserException, EvalException {
        assertEquals(true, new Parser("5=5").parseExpression().value(new Context()));
        assertEquals(true, new Parser("\"Hello\"=\"Hello\"").parseExpression().value(new Context()));
        assertEquals(false, new Parser("\"Hello\"=\"World\"").parseExpression().value(new Context()));
        assertEquals(false, new Parser("5!=5").parseExpression().value(new Context()));
        assertEquals(false, new Parser("\"Hello\"!=\"Hello\"").parseExpression().value(new Context()));
        assertEquals(true, new Parser("\"Hello\"!=\"World\"").parseExpression().value(new Context()));

        assertEquals(false, new Parser("5<5").parseExpression().value(new Context()));
        assertEquals(true, new Parser("4<5").parseExpression().value(new Context()));
        assertEquals(false, new Parser("5>5").parseExpression().value(new Context()));
        assertEquals(false, new Parser("4>5").parseExpression().value(new Context()));
    }


    public void testParseExpressionBool() throws IOException, ParserException, EvalException {
        assertEquals(3L, new Parser("1|2").parseExpression().value(new Context()));
        assertEquals(true, new Parser("a|b").parseExpression()
                .value(new Context()
                        .setVar("a", true)
                        .setVar("b", false)));
        assertEquals(0L, new Parser("1&2").parseExpression().value(new Context()));
        assertEquals(false, new Parser("a&b").parseExpression()
                .value(new Context()
                        .setVar("a", true)
                        .setVar("b", false)));
        assertEquals(3L, new Parser("1^2").parseExpression()
                .value(new Context()));
        assertEquals(true, new Parser("a^b").parseExpression()
                .value(new Context()
                        .setVar("a", true)
                        .setVar("b", false)));
        assertEquals(-2L, new Parser("~1").parseExpression().value(new Context()));
        assertEquals(false, new Parser("~a").parseExpression()
                .value(new Context()
                        .setVar("a", true)));

        assertEquals("true", new Template("<? if (1) print(\"true\"); ?>").execute().toString());
        assertEquals("", new Template("<? if (0) print(\"true\"); ?>").execute().toString());
    }

    public void testParseTemplateSimple() throws IOException, ParserException, EvalException {
        Context c = new Template("Hello World!").execute();
        assertEquals("Hello World!", c.toString());
    }

    public void testParseTemplateVariable() throws IOException, ParserException, EvalException {
        Context c = new Template("Hello <? =a ?> World!").execute(new Context().setVar("a", "My"));
        assertEquals("Hello My World!", c.toString());
    }

    public void testParseTemplateCodeOnly() throws IOException, ParserException, EvalException {
        Context c = new Template("<? =a ?>").execute(new Context().setVar("a", "My"));
        assertEquals("My", c.toString());
    }

    public void testParseTemplatePrint() throws IOException, ParserException, EvalException {
        Context c = new Template("Hello <? print(\"My\"); ?> World!").execute();
        assertEquals("Hello My World!", c.toString());
    }

    public void testParseTemplateFor() throws IOException, ParserException, EvalException {
        Context c = new Template("Hello <? for (i=0;i<10;i++) print(i); ?> World!").execute();
        assertEquals("Hello 0123456789 World!", c.toString());
    }

    public void testParseTemplateForStatements() throws IOException, ParserException, EvalException {
        Context c = new Template("<? for (i=0;i<10;i++) { print(i, 9-i); } ?>").execute();
        assertEquals("09182736455463728190", c.toString());
    }

    public void testParseTemplateForNested() throws IOException, ParserException, EvalException {
        Context c = new Template("Hello <? for (i=0;i<10;i++) { ?>n<? } ?> World!").execute();
        assertEquals("Hello nnnnnnnnnn World!", c.toString());
    }

    public void testParseTemplateForNested2() throws IOException, ParserException, EvalException {
        Context c = new Template("Hello <? for (i=0;i<3;i++) { ?>(<? for(j=0;j<2;j++) { ?>:<? } ?>)<? } ?> World!").execute();
        assertEquals("Hello (::)(::)(::) World!", c.toString());
    }

    public void testParseTemplateForNested3() throws IOException, ParserException, EvalException {
        Context c = new Template("Hello <? for (i=1;i<4;i++) { ?>(<? for(j=1;j<3;j++) { print(i*j); } ?>)<? } ?> World!").execute();
        assertEquals("Hello (12)(24)(36) World!", c.toString());
    }


    public void testParseTemplateElementAttibutes() throws IOException, ParserException, EvalException {
        ElementAttributes attr = new ElementAttributes().set(Keys.BITS, 5);
        Context c = new Context().setVar("elem", attr);
        new Template("bits=<?=elem.Bits?>;").execute(c);
        assertEquals("bits=5;", c.toString());
    }

    public void testParseTemplateFormat() throws IOException, ParserException, EvalException {
        ElementAttributes attr = new ElementAttributes().set(Keys.BITS, 17);
        Context c = new Context().setVar("elem", attr);
        new Template("<? a=format(\"hex=%x;\",elem.Bits); print(a);?>").execute(c);
        assertEquals("hex=11;", c.toString());
    }

    public void testParseTemplateArray() throws IOException, ParserException, EvalException {
        Context c = new Context().setVar("a", new ArrayList());
        new Template("<? a[0]=1; a[1]=7; print(a[1]); ?>;").execute(c);
        assertEquals("7;", c.toString());
        Object lo = c.getVar("a");
        assertTrue(lo instanceof List);
        List l = (List) lo;
        assertEquals(2, l.size());
        assertEquals(1L, l.get(0));
        assertEquals(7L, l.get(1));
    }

    public void testParseTemplateMap() throws IOException, ParserException, EvalException {
        Context c = new Template("<? m=newMap(); m.test=newMap(); m.test.val=7; print(m.test.val); ?>;").execute();
        assertEquals("7;", c.toString());
        Object mo = c.getVar("m");
        assertTrue(mo instanceof Map);
        mo = ((Map) mo).get("test");
        assertTrue(mo instanceof Map);
        assertEquals(7L, ((Map) mo).get("val"));
    }

    public void testParseTemplateIsSet() throws IOException, ParserException, EvalException {
        Template t = new Template("<? if (isset(m)) print(m); else print(\"false\"); ?>;");
        assertEquals("false;", t.execute().toString());
        assertEquals("4;", t.execute(new Context().setVar("m", 4)).toString());
    }

    public void testComment() throws IOException, ParserException, EvalException {
        Template t = new Template("<? // comment\nprint(\"false\"); // zzz\n ?>;");
        assertEquals("false;", t.execute().toString());
    }


//    public void testCode() throws IOException, ParserException {
//        File dir = new File("/home/hneemann/temp/Digital/ideras/Digital/src/main/resources/verilog");
//        for (File f : dir.listFiles()) {
//            try (Reader r = new FileReader(f)) {
//                System.out.println(f);
//                new Template(r);
//            }
//        }
//    }
}