
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

        /**
        if (!result.equals(expected + "\n")) {
            System.out.println("node is " + actual);
            System.out.println("Children: ");
            for (StatementParse child: ((StatementParse) actual).getChildren()){
                System.out.println(child);
            }
            //throw new AssertionError("Parsing \"" + str + "\"; expected " + expected + " but got " + result);
        }*/
    }

    public static void main(String[] args) {
        //test();
        Parser parser = new Parser();

        test(parser, "var is_prime = func(n) {\n" +
                "    var i = 2;\n" +
                "    while (i * i <= n) {\n" +
                "        var factor = n / i;\n" +
                "        if (i * factor == n) {\n" +
                "            ret 0;\n" +
                "        }\n" +
                "        i = i + 1;\n" +
                "    }\n" +
                "    ret 1;\n" +
                "};\n" +
                "\n" +
                "var get_nth_prime = func(n) {\n" +
                "    var i = 1;\n" +
                "    var count = 0;\n" +
                "    while (count < n) {\n" +
                "        i = i + 1;\n" +
                "        if (is_prime(i) == 1) {\n" +
                "            count = count + 1;\n" +
                "        }\n" +
                "    }\n" +
                "    ret i;\n" +
                "};\n" +
                "\n" +
                "print get_nth_prime(5);",0);
    }
}
