
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

        test(parser,
                "var BinarySearchTree = class {\n" +
                        "\n" +
                        "    var Node = class {\n" +
                        "        var value = 0;\n" +
                        "        var left = 0;\n" +
                        "        var right = 0;\n" +
                        "        var constructor = func(this, value) {\n" +
                        "            this.value = value;\n" +
                        "            ret this;\n" +
                        "        };\n" +
                        "    };\n" +
                        "\n" +
                        "    var node = Node();\n" +
                        "\n" +
                        "\n" +
                        "};\n" +
                        "\n" +
                        "var tree = BinarySearchTree();\n" +
                        "tree.node.value = 3;\n" +
                        "var inner = tree.node;\n" +
                        "print inner.value;",0);
    }
}
