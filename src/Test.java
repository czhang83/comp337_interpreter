
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

        if (!result.equals(String.valueOf(expected) + "\n")) {
            System.out.println("node is " + actual);
            System.out.println("Children: ");
            for (StatementParse child: ((StatementParse) actual).getChildren()){
                System.out.println(child);
            }
            //throw new AssertionError("Parsing \"" + str + "\"; expected " + expected + " but got " + result);
        }
    }

    public static void test(){
        Parser parser = new Parser();
        // integer tests
        test(parser,"3", 3);
        test(parser,"0", 0);
        test(parser,"100", 100);
        test(parser,"2021", 2021);
        //additional tests
        test(parser, "b", 0);
        test(parser, "", 0);
        test(parser, "3-", 3);
        test(parser, "3++", 3);
        test(parser, "3+4", 7);
        test(parser, "2020+2021", 4041);
        test(parser, "0+0", 0);
        test(parser, "1+1-", 2);
        test(parser, "1+1+-", 2);
        test(parser, "0+0+0+0+0", 0);
        test(parser, "42+0", 42);
        test(parser, "0+42", 42);
        test(parser, "123+234+345", 702);
        //parenthesis tests
        test(parser, "(0)",0);
        test(parser, "(0+0)", 0);
        test(parser, "(1+2)", 3);
        test(parser, "(1+2+3)", 6);
        test(parser, "4+(1+2+3)", 10);
        test(parser, "(1+2+3)+5", 11);
        test(parser, "4+(1+2+3)+5", 15);
        test(parser, "3+4+(5+6)+9", 27);

        //end-to-end test
        test(parser, "(3+4)+((2+3)+0+(1+2+3))+9", 27);

        //should fail
        test(parser, "1+1+b", 2);

        //opt_space test
        test(parser, "", 0);
        test(parser, " ", 0);
        test(parser, "   ", 0);

        test(parser, "3 + 4", 7);
        test(parser, "3  +  4", 7);
        //test(parser, " 3 + 4 ", "addition",new Parse(7,7)); //not in grammar
        test(parser, "(3 + 4)", 7);
        test(parser, "( 3+4 )", 7);
        test(parser, "4 +( 1+2+ 3)+ 5", 15);
        test(parser, "3 +(4+ (5+ 6)+9)", 27);

        //mul_div tests
        test(parser, "b", 0);
        test(parser, "", 0);
        test(parser, "3*", 3);
        test(parser, "3//", 3);
        test(parser, "3*4", 12);
        test(parser, "3/4", 0);
        test(parser, "20*25", 500);
        test(parser, "50/25", 2);
        test(parser, "0*0", 0);
        test(parser, "1*1/", 1);
        test(parser, "1*1*/", 1);
        test(parser, "1 *3 *(4* 5)", 60);
        test(parser, "42*0", 0);
        test(parser, "1 *(3 *(4* 5))", 60);
        test(parser, "5 *3 /(3* 1)", 5);

        //add_sub test
        test(parser, "5 -3 /(3* 1)", 4);
        test(parser, "3*4/6", 2);
        test(parser, "3 + 4 - 5", 2);
        test(parser, "3 + (4 * 5)", 23);
        test(parser, "3 * 4 - 5", 7);

        // print
        test(parser, "print 1-2-3 + 10/5/2;", -3);
    }
    public static void main(String[] args) {
        //test();
        Parser parser = new Parser();
        test(parser, "print (1*(2+(3*(4+(5*(6+(7*(8+(9*(10+(11*(12+(13*(14+(15*(16+(17*(18+(19*20)))))))))))))))))));", 0);
    }
}
