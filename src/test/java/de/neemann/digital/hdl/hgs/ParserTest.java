/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import de.neemann.digital.hdl.hgs.function.Function;
import de.neemann.digital.hdl.hgs.function.JavaClass;
import de.neemann.digital.integration.FileScanner;
import de.neemann.digital.integration.Resources;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ParserTest extends TestCase {

    private Context exec(String code) throws IOException, ParserException, HGSEvalException {
        return exec(code, new Context());
    }

    private Context exec(String code, Context c) throws IOException, ParserException, HGSEvalException {
        new Parser(code).parse().execute(c);
        return c;
    }

    private Context exec(Statement s) throws HGSEvalException {
        return exec(s, new Context());
    }

    private Context exec(Statement s, Context c) throws HGSEvalException {
        s.execute(c);
        return c;
    }

    private void failToParseExp(String code) throws IOException {
        try {
            new Parser(code).parseExp();
            fail();
        } catch (ParserException e) {
            // expected to fail
        }
    }

    private void failToEval(String code, Context c) throws IOException, ParserException {
        try {
            exec(code, c);
            fail();
        } catch (HGSEvalException e) {
            // expected to fail
        }
    }


    public void testParseExpArith() throws IOException, ParserException, HGSEvalException {
        assertEquals(3L, new Parser("1+2").parseExp().value(new Context()));
        assertEquals("HelloWorld", new Parser("\"Hello\"+\"World\"").parseExp().value(new Context()));
        assertEquals(3L, new Parser("5-2").parseExp().value(new Context()));
        assertEquals(10L, new Parser("5*2").parseExp().value(new Context()));
        assertEquals(2L, new Parser("6/3").parseExp().value(new Context()));
        assertEquals(8L, new Parser("1<<3").parseExp().value(new Context()));
        assertEquals(2L, new Parser("16>>3").parseExp().value(new Context()));
        assertEquals(4L, new Parser("9%5").parseExp().value(new Context()));

        assertEquals(100L, new Parser("20/2*10").parseExp().value(new Context()));
        assertEquals(20L, new Parser("20*10/10").parseExp().value(new Context()));
        assertEquals(10L, new Parser("200/2/10").parseExp().value(new Context()));
        assertEquals(10.0, new Parser("200/2.0/10").parseExp().value(new Context()));

        assertEquals(-5L, new Parser("-5").parseExp().value(new Context()));
        assertEquals(6L, new Parser("2*(1+2)").parseExp().value(new Context()));
        assertEquals(5L, new Parser("2*2+1").parseExp().value(new Context()));
        assertEquals(5L, new Parser("1+2*2").parseExp().value(new Context()));
        assertEquals(2L, new Parser("2/2+1").parseExp().value(new Context()));
        assertEquals(2L, new Parser("1+2/2").parseExp().value(new Context()));
        assertEquals(1L, new Parser("--1").parseExp().value(new Context()));

        failToParseExp("1+");
        failToParseExp("1+(a");

        assertEquals("Hallo4", new Parser("\"Hallo\" + (2*2)").parseExp().value(new Context()));
        assertEquals("Hallo_true", new Parser("\"Hallo_\" + (1<2)").parseExp().value(new Context()));
    }

    public void testParseExpArithDouble() throws IOException, ParserException, HGSEvalException {
        assertEquals(4.0, new Parser("1.5+2.5").parseExp().value(new Context()));
        assertEquals(0.5, new Parser("1.5-1").parseExp().value(new Context()));
        assertEquals(3.0, new Parser("1.5*2").parseExp().value(new Context()));
        assertEquals(3.0, new Parser("2*1.5").parseExp().value(new Context()));
        assertEquals(1L, new Parser("3/2").parseExp().value(new Context()));
        assertEquals(1.5, new Parser("3.0/2").parseExp().value(new Context()));
        assertEquals(1.5, new Parser("3/2.0").parseExp().value(new Context()));
        assertEquals(1.5, new Parser("3.0/2.0").parseExp().value(new Context()));
        assertEquals(true, new Parser("1.0001>1").parseExp().value(new Context()));
        assertEquals(false, new Parser("1>1.0001").parseExp().value(new Context()));
        assertEquals(false, new Parser("1.0001<1").parseExp().value(new Context()));
        assertEquals(true, new Parser("1<1.0001").parseExp().value(new Context()));

        assertEquals(2L, new Parser("floor(2.5)").parseExp().value(new Context()));
        assertEquals(3L, new Parser("ceil(2.5)").parseExp().value(new Context()));
        assertEquals(3L, new Parser("round(2.8)").parseExp().value(new Context()));
        assertEquals(4.0, new Parser("float(4)").parseExp().value(new Context()));

        assertEquals(2L, new Parser("min(2,3)").parseExp().value(new Context()));
        assertEquals(2.5, new Parser("min(2.5,3)").parseExp().value(new Context()));
        assertEquals(2.5, new Parser("min(2.5,3.5)").parseExp().value(new Context()));
        assertEquals(3L, new Parser("max(2,3)").parseExp().value(new Context()));
        assertEquals(3.0, new Parser("max(2.5,3)").parseExp().value(new Context()));
        assertEquals(3.5, new Parser("max(2.5,3.5)").parseExp().value(new Context()));

        assertEquals(3.5, new Parser("abs(3.5)").parseExp().value(new Context()));
        assertEquals(3.5, new Parser("abs(-3.5)").parseExp().value(new Context()));
        assertEquals(3L, new Parser("abs(3)").parseExp().value(new Context()));
        assertEquals(3L, new Parser("abs(-3)").parseExp().value(new Context()));
    }

    public void testParseExpCompare() throws IOException, ParserException, HGSEvalException {
        assertEquals(true, new Parser("5=5").parseExp().value(new Context()));
        assertEquals(true, new Parser("\"Hello\"=\"Hello\"").parseExp().value(new Context()));
        assertEquals(false, new Parser("\"Hello\"=\"World\"").parseExp().value(new Context()));
        assertEquals(false, new Parser("5!=5").parseExp().value(new Context()));
        assertEquals(false, new Parser("\"Hello\"!=\"Hello\"").parseExp().value(new Context()));
        assertEquals(true, new Parser("\"Hello\"!=\"World\"").parseExp().value(new Context()));
        assertEquals(true, new Parser("\"a\"<\"b\"").parseExp().value(new Context()));
        assertEquals(false, new Parser("\"b\"<\"a\"").parseExp().value(new Context()));
        assertEquals(false, new Parser("\"a\">\"b\"").parseExp().value(new Context()));
        assertEquals(true, new Parser("\"b\">\"a\"").parseExp().value(new Context()));

        assertEquals(false, new Parser("5<5").parseExp().value(new Context()));
        assertEquals(true, new Parser("4<5").parseExp().value(new Context()));
        assertEquals(false, new Parser("5>5").parseExp().value(new Context()));
        assertEquals(false, new Parser("4>5").parseExp().value(new Context()));

        assertEquals(false, new Parser("4>=5").parseExp().value(new Context()));
        assertEquals(true, new Parser("5>=5").parseExp().value(new Context()));
        assertEquals(true, new Parser("6>=5").parseExp().value(new Context()));

        assertEquals(true, new Parser("4<=5").parseExp().value(new Context()));
        assertEquals(true, new Parser("5<=5").parseExp().value(new Context()));
        assertEquals(false, new Parser("6<=5").parseExp().value(new Context()));
    }


    public void testParseExpBool() throws IOException, ParserException, HGSEvalException {
        assertEquals(3L, new Parser("1|2").parseExp().value(new Context()));
        assertEquals(true, new Parser("a|b").parseExp()
                .value(new Context()
                        .declareVar("a", true)
                        .declareVar("b", false)));
        assertEquals(0L, new Parser("1&2").parseExp().value(new Context()));
        assertEquals(false, new Parser("a&b").parseExp()
                .value(new Context()
                        .declareVar("a", true)
                        .declareVar("b", false)));
        assertEquals(3L, new Parser("1^2").parseExp()
                .value(new Context()));
        assertEquals(true, new Parser("a^b").parseExp()
                .value(new Context()
                        .declareVar("a", true)
                        .declareVar("b", false)));
        assertEquals(-2L, new Parser("~1").parseExp().value(new Context()));
        assertEquals(false, new Parser("~a").parseExp()
                .value(new Context()
                        .declareVar("a", true)));

        assertEquals("true", exec("<? if (1) print(\"true\"); ?>").toString());
        assertEquals("", exec("<? if (0) print(\"true\"); ?>").toString());
        assertEquals("true", exec("<? if (1=1) print(\"true\"); ?>").toString());
        assertEquals("", exec("<? if (0=1) print(\"true\"); ?>").toString());
        assertEquals("true", exec("<? a:=true; print(a); ?>").toString());
        assertEquals("false", exec("<? a:=true; a=false; print(a); ?>").toString());
    }

    public void testTypeChecking() throws IOException, ParserException, HGSEvalException {
        assertEquals("5.0", exec("<? a:=4.0; a=5.0; print(a); ?>", new Context()).toString());
        assertEquals("u", exec("<? a:=\"zz\"; a=\"u\"; print(a); ?>", new Context()).toString());

        failToEval("<? a:=\"zz\"; a=5; print(a); ?>", new Context());
        failToEval("<? a:=5; a=5.0; print(a); ?>", new Context());
        failToEval("<? a:=5.0; a=5; print(a); ?>", new Context());
    }

    public void testParseTemplateSimple() throws IOException, ParserException, HGSEvalException {
        assertEquals("Hello World!", exec("Hello World!", new Context()).toString());
    }

    public void testParseTemplateTextOnly() throws IOException, ParserException, HGSEvalException {
        assertEquals("Hello World!", exec("Hello World!").toString());
        assertEquals("Hello < < World!", exec("Hello < < World!").toString());
    }

    public void testParseCommandsOnly() throws IOException, ParserException, HGSEvalException {
        Statement s = new Parser("a:=2; b:=a*a;").parse(false);
        Context context = new Context();
        s.execute(context);
        assertEquals(4L, context.getVar("b"));
    }

    public void testParseTemplateVariable() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? =a ?> World!", new Context().declareVar("a", "My"));
        assertEquals("Hello My World!", c.toString());
    }

    public void testParseTemplateVariableEscape() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? ='a a' ?> World!", new Context().declareVar("a a", "My"));
        assertEquals("Hello My World!", c.toString());
    }

    public void testParseTemplateCodeOnly() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? =a ?>", new Context().declareVar("a", "My"));
        assertEquals("My", c.toString());
        c = exec("{? =a ?}", new Context().declareVar("a", "My"));
        assertEquals("My", c.toString());
    }

    public void testParseTemplatePrint() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? print(\"My\"); ?> World!");
        assertEquals("Hello My World!", c.toString());
        c = exec("Hello {? print(\"My\"); ?} World!");
        assertEquals("Hello My World!", c.toString());

        failToEval("Hello <? a=print(\"My\"); ?> World!", new Context());
    }

    public void testParseTemplatePrintf() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? printf(\"-%d-%d-\", 4, 5); ?> World!");
        assertEquals("Hello -4-5- World!", c.toString());
    }

    //   if statement

    public void testParseTemplateIf() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? b:=9; if (a<1) b=0; else b=1; print(b);?>", new Context().declareVar("a", 0));
        assertEquals("0", c.toString());
        assertTrue(c.contains("b"));

        c = exec("<? if (a<1) export b:=0; else export b:=1; print(b);?>", new Context().declareVar("a", 0));
        assertEquals("0", c.toString());
        assertTrue(c.contains("b"));
    }
    //   for statement

    public void testParseTemplateFor() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? for (i:=0;i<10;i++) print(i); ?> World!");
        assertEquals("Hello 0123456789 World!", c.toString());

        c = exec("Hello <? for (i:=9;i>=0;i--) print(i); ?> World!");
        assertEquals("Hello 9876543210 World!", c.toString());

        c = exec("<? " +
                "a:=newList(); " +
                "for (i:=0;i<10;i++) a[i]:=i; " +
                "for (i:=0;i<10;i++) a[i]=9-i; " +
                "for (i:=0;i<10;i++) print(a[i]); " +
                "?>");
        assertEquals("9876543210", c.toString());

        c = exec("<? " +
                "a:=newList(); " +
                "for (i:=0;i<5;i++) {" +
                "  for (j:=0;j<5;j++) a[i*5+j]:=i*j; " +
                "}" +
                "print(sizeOf(a));" +
                "for (i:=0;i<sizeOf(a);i++) print(\",\", a[i]); " +
                "?>");
        assertEquals("25,0,0,0,0,0,0,1,2,3,4,0,2,4,6,8,0,3,6,9,12,0,4,8,12,16", c.toString());

    }

    public void testParseTemplateForStatements() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? for (i:=0;i<10;i++) { print(i, 9-i); } ?>");
        assertEquals("09182736455463728190", c.toString());
    }

    public void testParseTemplateForNested() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? for (i:=0;i<10;i++) { ?>n<? } ?> World!");
        assertEquals("Hello nnnnnnnnnn World!", c.toString());
    }

    public void testParseTemplateForNested2() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? for (i:=0;i<3;i++) { ?>(<? for(j:=0;j<2;j++) { ?>:<? } ?>)<? } ?> World!");
        assertEquals("Hello (::)(::)(::) World!", c.toString());
    }

    public void testParseTemplateForNested3() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? for (i:=1;i<4;i++) { ?>(<? for(j:=1;j<3;j++) { print(i*j); } ?>)<? } ?> World!");
        assertEquals("Hello (12)(24)(36) World!", c.toString());
    }

    //   while statement

    public void testParseTemplateWhile() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? i:=0; while (i<=9) { =i; i++; } ?> World!");
        assertEquals("Hello 0123456789 World!", c.toString());
    }

    public void testParseTemplateRepeat() throws IOException, ParserException, HGSEvalException {
        Context c = exec("Hello <? i:=0; repeat { =i; i++; } until i=10; ?> World!");
        assertEquals("Hello 0123456789 World!", c.toString());
    }

    public void testParseTemplateArray() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? a:=newList(); a[0]:=1; a[1]:=7; print(a[1], \",\" ,sizeOf(a)); ?>;");
        assertEquals("7,2;", c.toString());
        Object lo = c.getVar("a");
        assertTrue(lo instanceof List);
        List l = (List) lo;
        assertEquals(2, l.size());
        assertEquals(1L, l.get(0));
        assertEquals(7L, l.get(1));
    }

    public void testParseTemplateMap() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? m:=newMap(); m.test:=newMap(); m.test.val:=7; print(m.test.val); ?>;");
        assertEquals("7;", c.toString());
        Object mo = c.getVar("m");
        assertTrue(mo instanceof Map);
        mo = ((Map) mo).get("test");
        assertTrue(mo instanceof Map);
        assertEquals(7L, ((Map) mo).get("val"));
    }

    public void testParseWrongAssignments() throws IOException, ParserException {
        failToEval("<? m=1; ?>;", new Context());
        failToEval("<? m:=1; m.test:=2; ?>;", new Context());
        failToEval("<? m:=1; m[0]:=2; ?>;", new Context());
    }

    public void testParseTemplateIsSet() throws IOException, ParserException, HGSEvalException {
        Statement t = new Parser("<? if (isPresent(m)) print(m); else print(\"false\"); ?>;").parse();
        assertEquals("false;", exec(t).toString());
        assertEquals("4;", exec(t, new Context().declareVar("m", 4)).toString());
    }

    public void testParseTemplateFormat() throws IOException, ParserException, HGSEvalException {
        Context c = new Context().declareVar("Bits", 17);
        exec("<? a:=format(\"hex=%x;\",Bits); print(a);?>", c);
        assertEquals("hex=11;", c.toString());

        c = new Context().declareVar("freq", Math.PI * 100);
        exec("<? a:=format(\"f=%.2f;\",freq); print(a);?>", c);
        assertEquals("f=314.16;", c.toString());
    }

    public void testParseTemplateEscape() throws IOException, ParserException, HGSEvalException {
        assertEquals("simple", exec("<? str:=\"simple\"; print(identifier(str)); ?>").toString());
        assertEquals("hp", exec("<? str:=\"hä-äpä\"; print(identifier(str)); ?>").toString());
        assertEquals("A0", exec("<? str:=\"A-0\"; print(identifier(str)); ?>").toString());
        assertEquals("n0A", exec("<? str:=\"0-A\"; print(identifier(str)); ?>").toString());
    }

    public void testComment() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? // comment\nprint(\"false\"); // zzz\n ?>;");
        assertEquals("false;", c.toString());
    }

    public void testAddFunction() throws IOException, ParserException, HGSEvalException {
        Statement s = new Parser("a : in <?=type(Bits)?>;").parse();
        Context funcs = new Context().declareVar("type", new Function(1) {
            @Override
            protected Object f(Object... args) throws HGSEvalException {
                int n = Value.toInt(args[0]);
                if (n == 1)
                    return "std_logic";
                else
                    return "std_logic_vector(" + (n - 1) + " downto 0)";
            }
        });
        assertEquals("a : in std_logic;",
                exec(s, new Context(funcs)
                        .declareVar("Bits", 1)).toString());
        assertEquals("a : in std_logic_vector(5 downto 0);",
                exec(s, new Context(funcs)
                        .declareVar("Bits", 6)).toString());
    }

    int flag = 0;

    public void testFunctionAsStatement() throws IOException, ParserException, HGSEvalException {
        flag = 0;
        Statement s = new Parser("a : in <? type(7); ?>;").parse();

        Context c = new Context().declareFunc("type", new Function(1) {
            @Override
            protected Object f(Object... args) throws HGSEvalException {
                flag = Value.toInt(args[0]);
                return null;
            }
        });
        assertEquals("a : in ;", exec(s, c).toString());
        assertEquals(7, flag);
    }

    public void testFunction() throws IOException, ParserException, HGSEvalException {
        assertEquals("18", exec("<? f:=func(a){return a*a+2;};  print(f(4));?>").toString());
        assertEquals("5", exec("<? f:=func(a,b){return a+2*b;};  print(f(1,2));?>").toString());
        assertEquals("13", exec("<? f:=func(a,b){return a+2*b;};  print(f(1,a*2));?>",
                new Context().declareVar("a", 3)).toString());

        assertEquals("18", exec("<? m:=newMap(); m.f:=func(a){m:=newMap(); m.v:=a*a+2; return m;};  print(m.f(4).v);?>").toString());
        assertEquals("18", exec("<? m:=newList(); m[0]:=func(a){ l:=newList(); l[0]:=a*a+2; return l;};  print(m[0](4)[0]);?>").toString());

        failToEval("<? return 1; ?>", new Context());
        failToEval("<? f:=func(a){return a;}; f(1)=5; ?>", new Context());
    }

    public void testFunctionOutput() throws IOException, ParserException, HGSEvalException {
        assertEquals("testtext12testtext15",
                exec("<? f:=func(a){  ?>testtext<? print(a*3); return output(); };  print(f(4),f(5));?>").toString());
    }

    public void testFunctionRecursion() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? \n" +
                "fibu:=func(n){\n" +
                "  if (n<2)\n" +
                "    return n;\n" +
                "  else\n" +
                "    return fibu(n-1)+fibu(n-2);\n" +
                "};\n" +
                "print(fibu(12));\n" +
                " ?>");
        assertEquals("144", c.toString());

        c = exec("<? \n" +
                "func fibu(n){\n" +
                "  if (n<2)\n" +
                "    return n;\n" +
                "  else\n" +
                "    return fibu(n-1)+fibu(n-2);\n" +
                "}\n" +
                "for (i:=0;i<=12;i++) print(fibu(i),\",\");" +
                " ?>");
        assertEquals("0,1,1,2,3,5,8,13,21,34,55,89,144,", c.toString());
    }

    public void testFunctionLambda() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? outer:=5; f:=func(x) {return x+outer;}; ?>");
        Function f = c.getFunction("f");
        assertEquals(6L, f.call(1));
        assertEquals(7L, f.call(2));

        c = exec("<? f:=func(x){ return func(u){return u*x;};}; a:=f(2); b:=f(5); ?>");
        Function a = c.getFunction("a");
        Function b = c.getFunction("b");
        assertEquals(4L, a.call(2));
        assertEquals(6L, a.call(3));
        assertEquals(10L, b.call(2));
        assertEquals(15L, b.call(3));
    }

    public void testFunctionClosure() throws IOException, ParserException, HGSEvalException {
        Context c = exec("<? inner:=0; inc:=func(){inner++; return inner;}; dec:=func(){inner--; return inner;};?>");
        Function inc = c.getFunction("inc");
        Function dec = c.getFunction("dec");
        assertEquals(1L, inc.call());
        assertEquals(2L, inc.call());
        assertEquals(3L, inc.call());
        assertEquals(2L, dec.call());
        assertEquals(1L, dec.call());
        assertEquals(0L, dec.call());
        assertEquals(1L, inc.call());
    }

    public void testFunctionClosure2() throws IOException, ParserException, HGSEvalException {
        String c = exec(
                "<?" +
                        "func create() {" +
                        "   inner:=0; " +
                        "   return func(){" +
                        "      inner++; " +
                        "      return inner;" +
                        "   };" +
                        "}" +
                        "a:=create();" +
                        "b:=create();" +
                        "print(a()+\",\"+a()+\",\"+b());" +
                        "?>").toString();

        assertEquals("1,2,1", c);
    }

    public static final class TestClass {
        private long inner;

        public void add(long n) {
            inner += n;
        }

        public void sub(long n) {
            inner -= n;
        }
    }

    public static final class TestClassStatic {
        private static long inner;

        public static void set(long n) {
            inner = n;
        }

        public static void add(long n) {
            inner += n;
        }

        public static void sub(long n) {
            inner -= n;
        }

        public static String concat(String... value) {
            String sum = "";
            for (int i = 0; i < value.length; i++)
                sum += value[i];
            return sum;
        }

        public static long mean(long... value) {
            long sum = 0;
            for (int i = 0; i < value.length; i++)
                sum += value[i];
            return sum / value.length;
        }

        public static String zzz(Context c, String name, long... value) {
            long sum = 0;
            for (int i = 0; i < value.length; i++)
                sum += value[i];
            return c.toString() + name + (sum / value.length);
        }
    }

    public void testJavaClass() throws ParserException, IOException, HGSEvalException {
        JavaClass<TestClass> jc = new JavaClass<>(TestClass.class);
        TestClass t = new TestClass();
        exec("<? z.add(6);z.sub(3); ?>",
                new Context().declareVar("z", jc.createMap(t)));
        assertEquals(3L, t.inner);

        JavaClass<TestClassStatic> jcs = new JavaClass<>(TestClassStatic.class);
        exec("<? z.set(0);z.add(6);z.sub(3); ?>",
                new Context().declareVar("z", jcs.createMap(null)));
        assertEquals(3L, TestClassStatic.inner);


        Context c = exec("<? print(z.concat(\"a\"), z.concat(\"a\",\"b\"), z.concat(\"a\",\"b\",\"c\")); ?>",
                new Context().declareVar("z", jcs.createMap(null)));
        assertEquals("aababc", c.toString());

        c = exec("<? print(z.mean(1),\",\",z.mean(3,5),\",\",z.mean(3,4,5)); ?>",
                new Context().declareVar("z", jcs.createMap(null)));
        assertEquals("1,4,4", c.toString());

        c = exec("Hello <? print(z.zzz(\"World \",5,7,9)); ?>",
                new Context().declareVar("z", jcs.createMap(null)));
        assertEquals("Hello Hello World 7", c.toString());
    }


    public void testPanic() throws IOException, ParserException, HGSEvalException {
        Statement s = new Parser("<? if (i>1) panic(\"myError\"); ?>").parse();

        exec(s, new Context().declareVar("i", 0));
        exec(s, new Context().declareVar("i", 1));

        try {
            exec(s, new Context().declareVar("i", 2));
            fail();
        } catch (HGSEvalException e) {
            assertEquals("myError; line 1", e.getMessage());
        }
    }

    public void testPanic2() throws IOException, ParserException {
        Statement s = new Parser("<? panic(\"err_varNotDefined_N\",\"hello\"); ?>").parse();

        try {
            exec(s);
            fail();
        } catch (HGSEvalException e) {
            assertTrue(e.getMessage().contains("hello"));
            assertFalse(e.getMessage().contains("err_"));
        }
    }

    public void testTrim() throws IOException, ParserException, HGSEvalException {
        assertEquals(" 5", exec(" <?=5;-?> ").toString());
        assertEquals("5 ", exec(" <?-=5?> ").toString());
        assertEquals("5", exec("\n\n  <?-=5;-?>\n\n").toString());
        assertEquals("\n 5 \n", exec("\n <?=5?> \n").toString());
        assertEquals("5", exec("\n\n  <?- print(5);-?>\n\n").toString());

        assertEquals(" 5", exec("<??> <?=5;-?> ").toString());
        assertEquals("5 ", exec("<??> <?-=5?> ").toString());
        assertEquals("5", exec("<??>\n\n  <?-=5;-?>\n\n").toString());
        assertEquals("\n 5 \n", exec("<??>\n <?=5?> \n").toString());
        assertEquals("5", exec("<??>\n\n  <?- print(5);-?>\n\n").toString());
    }

    // checks the available VHDL templates
    public void testVHDLTemplates() throws Exception {
        final File path = new File(Resources.getRoot(), "../../main/resources/vhdl");
        int n = new FileScanner(f -> new Parser(new FileReader(f), f.getName()).parse()).setSuffix(".tem").scan(path);
        assertTrue(n > 10);
    }

    // checks the available verilog templates
    public void testVerilogTemplates() throws Exception {
        final File path = new File(Resources.getRoot(), "../../main/resources/verilog");
        int n = new FileScanner(f -> new Parser(new FileReader(f), f.getName()).parse()).setSuffix(".v").scan(path);
        assertTrue(n > 10);
    }

}