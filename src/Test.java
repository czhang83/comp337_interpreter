
public class Test {

    private static void test(Parser parser, String str, int expected) {
        Interpreter interpreter = new Interpreter();
        ConstantFoldingTransform transform = new ConstantFoldingTransform();
        System.out.println(str);
        Parse actual = parser.parse(str);
        System.out.println("Before transform: ");
        System.out.println(actual);
        actual = transform.visit(actual);
        System.out.println("After transform: ");
        System.out.println(actual);
        if (actual == null) {
            throw new AssertionError("Got null when parsing \"" + str + "\"");
        }
        System.out.println("s expression: " + actual);
        String result = interpreter.execute(actual);
        System.out.println("result: " + result);
    }

    public static void main(String[] args) {
        //test();
        Parser parser = new Parser();

        test(parser,
                "5 - 2 * func(n){6/3+n;} + 1;",0);
    }
}
