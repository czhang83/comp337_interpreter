
public class Test {

    private static void test(Parser parser, String str, int expected) {
        Interpreter interpreter = new Interpreter();
        System.out.println(str);
        Parse actual = parser.parse(str);
        if (actual == null) {
            throw new AssertionError("Got null when parsing \"" + str + "\"");
        }
        System.out.println("s expression: " + actual);
        String result = interpreter.execute(actual);
        System.out.println("result: " + result);

        if (!result.equals(expected + "\n")) {
            System.out.println("node is " + actual);
            System.out.println("Children: ");
            for (StatementParse child: ((StatementParse) actual).getChildren()){
                System.out.println(child);
            }
            //throw new AssertionError("Parsing \"" + str + "\"; expected " + expected + " but got " + result);
        }
    }

    public static void main(String[] args) {
        //test();
        Parser parser = new Parser();
        test(parser, "var func1 = func(){\n" +
                "        var func2 = func(){\n" +
                "            var func3 = func(){\n" +
                "            print 4;\n" +
                "            };\n" +
                "        ret func3;\n" +
                "    };\n" +
                "    ret func2;\n" +
                "};\n" +
                "\n" +
                "func1()()();",0);
    }
}
