

public class Parser {

    static Parse FAIL = new Parse(0, -1); // a correct one with never produce -1 index

    // wrapper
    public Parse parse(String str, String term){
        return this.parse(str, 0, term);
    }
    // redirect parse to other corresponding parse methods
    private Parse parse(String str, int index, String term ){
        if(index >= str.length()){
            return Parser.FAIL;
        }
        if (term.equals("integer")){
            return this.parse_integer(str, index);
        } else if (term.equals("addition")){
            return this.parse_addition_expression(str, index);
        } else if (term.equals("operand")){
            return this.parse_operand(str, index);
        } else if (term.equals("parenthesis")){
            return this.parse_parenthesis(str, index);
        } else{
            throw new AssertionError("Unexpected term " + term);
        }
    }
    private Parse parse_operand(String str, int index){ //either integer or parenthesis
        Parse parse = this.parse(str, index, "integer");
        if (!parse.equals(Parser.FAIL)){
            return parse;
        }
        parse = this.parse(str, index,"parenthesis");
        if(!parse.equals(Parser.FAIL)){
            return parse;
        }
        return Parser.FAIL;
    }
    private Parse parse_parenthesis(String str, int index){
        if (str.charAt(index) != '('){
            return Parser.FAIL;
        }
        Parse parse = this.parse(str, index+1, "addition");
        if (parse.equals(Parser.FAIL)){
            return Parser.FAIL;
        }
        if (str.charAt(parse.getIndex()) != ')'){
            return Parser.FAIL;
        }
        return new Parse(parse.getValue(), parse.getIndex() + 1);
    }
    private Parse parse_addition_expression(String str, int index){
        //always start with an integer
        Parse parse = this.parse(str, index, "operand"); //parse out the first integer
        int result = 0;
        if (parse.equals(Parser.FAIL)){ // if not start a with integer, fail
            return Parser.FAIL;
        }
        result = parse.getValue();
        index = parse.getIndex();
        while (index < str.length() && !parse.equals(Parser.FAIL)){
            if (str.charAt(index) != '+'){ // the integer follow by +
                parse = Parser.FAIL;
                break;
            }
            parse = this.parse(str, parse.getIndex() + 1, "operand");
            if (parse.equals(Parser.FAIL)){
                parse = Parser.FAIL;
                break;
            }
            result += parse.getValue();
            index = parse.getIndex();

        }
        return new Parse(result, index);
    }
    private Parse parse_integer(String str, int index){
        String parsed = "";
        while (index < str.length() && Character.isDigit(str.charAt(index))){
            parsed += str.charAt(index);
            index++;
        }
        if (parsed.equals("")){ //not having any integer
            return Parser.FAIL;
        }
        return new Parse(Integer.parseInt(parsed), index);
    }
    private static void test(Parser parser, String str, String term, Parse expected) {
        Parse actual = parser.parse(str, term);
        if (actual == null) {
            throw new AssertionError("Got null when parsing \"" + str + "\"");
        }
        if (!actual.equals(expected)) {
            throw new AssertionError("Parsing \"" + str + "\"; expected " + expected + " but got " + actual);
        }
    }

    public static void test(){
        Parser parser = new Parser();
        // integer tests
        test(parser,"3", "integer", new Parse(3,1));
        test(parser,"0", "integer",new Parse(0,1));
        test(parser,"100", "integer",new Parse(100,3));
        test(parser,"2021", "integer",new Parse(2021,4));
        test(parser, "b", "integer", Parser.FAIL);
        test(parser, "", "integer", Parser.FAIL);
        //additional tests
        test(parser, "b", "addition", Parser.FAIL);
        test(parser, "", "addition", Parser.FAIL);
        test(parser, "3-", "addition", new Parse(3,1));
        test(parser, "3++", "addition", new Parse(3,1));
        test(parser, "3+4", "addition", new Parse(7,3));
        test(parser, "2020+2021", "addition",new Parse(4041,9));
        test(parser, "0+0", "addition",new Parse(0,3));
        test(parser, "1+1-", "addition",new Parse(2,3));
        test(parser, "1+1+-", "addition",new Parse(2,3));
        test(parser, "0+0+0+0+0", "addition",new Parse(0,9));
        test(parser, "42+0", "addition",new Parse(42,4));
        test(parser, "0+42", "addition",new Parse(42,4));
        test(parser, "123+234+345", "addition",new Parse(702,11));
        //parenthesis tests
        test(parser, "(0)", "parenthesis",new Parse(0,3));
        test(parser, "(0+0)", "parenthesis",new Parse(0,5));
        test(parser, "(1+2)", "parenthesis",new Parse(3,5));
        test(parser, "(1+2+3)", "parenthesis",new Parse(6,7));
        test(parser, "4+(1+2+3)", "addition",new Parse(10,9));
        test(parser, "(1+2+3)+5", "addition",new Parse(11,9));
        test(parser, "4+(1+2+3)+5", "addition",new Parse(15,11));
        test(parser, "3+4+(5+6)+9", "addition",new Parse(27,11));

        //end-to-end test
        test(parser, "(3+4)+((2+3)+0+(1+2+3))+9", "addition", new Parse(27,25));

        //should fail
        test(parser, "1+1+b", "addition",new Parse(2,3));


    }
    public static void main(String[] args) {
        test();
    }

}