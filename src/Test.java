
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
                        "    \n" +
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
                        "    var root = 0;\n" +
                        "\n" +
                        "    var constructor = func(this) {\n" +
                        "        ret this;\n" +
                        "    };\n" +
                        "\n" +
                        "    var add = func(this, value) {\n" +
                        "        root = recur_add(root, value);\n" +
                        "    };\n" +
                        "\n" +
                        "    var recur_add = func(this, node, value) {\n" +
                        "        if (node == 0) {\n" +
                        "            ret Node().constructor(value);\n" +
                        "        }\n" +
                        "        if (value <= node.value) {\n" +
                        "            node.left = this.recur_add(node.left, value);\n" +
                        "            ret node;\n" +
                        "        }\n" +
                        "        if (value > node.value) {\n" +
                        "            node.right = recur_add(node.right, value);\n" +
                        "            ret node;\n" +
                        "        }\n" +
                        "    };\n" +
                        "\n" +
                        "    var in_order_print = func(this) {\n" +
                        "        recur_in_order_print(root);\n" +
                        "    };\n" +
                        "\n" +
                        "    var recur_in_order_print = func(this, node) {\n" +
                        "        if (node != 0) {\n" +
                        "            recur_in_order_print(node.left);\n" +
                        "            print node.value;\n" +
                        "            recur_in_order_print(node.right);\n" +
                        "        }\n" +
                        "    };\n" +
                        "\n" +
                        "};\n" +
                        "\n" +
                        "var tree = BinarySearchTree().constructor();\n" +
                        "\n" +
                        "tree.add(9);\n" +
                        "tree.add(7);\n" +
                        "tree.add(0);\n" +
                        "tree.add(1);\n" +
                        "tree.add(3);\n" +
                        "tree.add(2);\n" +
                        "tree.add(6);\n" +
                        "tree.add(8);\n" +
                        "tree.add(4);\n" +
                        "tree.add(5);\n" +
                        "\n" +
                        "tree.in_order_print();",0);
    }
}
