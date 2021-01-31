import java.util.ArrayList;
import java.util.Arrays;
import java.util.List; // new ArrayList<>(Arrays.asList(meat, salad, dessert)

// index increment inside parse_integer
// current index = number of character read, and the string position for next character
// use index directly in methods, increment only when read in characters
public class Parser {

    static Parse FAIL = new Parse(0, -1); // a correct one with never produce -1 index

    // wrapper
    public Parse parse(String str, String term){
        return this.parse(str, 0, term);
    }
    // redirect parse to other corresponding parse methods
    private Parse parse(String str, int index, String term ){
        if(index > str.length()){ // allow empty string/check space at end - check empty string in parse functions
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
        } else if (term.equals("opt_space")){ //opt_space can take empty string (index = str.length())
            return this.parse_opt_space(str, index);
        } else if (term.equals("add_sub_operator")){
            return this.parse_add_sub_operator(str, index);
        } else if (term.equals("mul_div_operator")){
            return this.parse_mul_div_operator(str, index);
        } else if (term.equals("mul_div_expression")){
            return this.parse_mul_div_expression(str, index);
        } else {
            throw new AssertionError("Unexpected term " + term);
        }
    }
    //either integer or parenthesis
    private Parse parse_operand(String str, int index){
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
    // ( opt_space addition opt_space )
    private Parse parse_parenthesis(String str, int index){
        if (index == str.length() || str.charAt(index) != '('){ // short circuit - check if empty string/index at end
            return Parser.FAIL;
        }
        Parse parse = this.parse(str, index+1, "opt_space");
        int index_before_expression = parse.getIndex();
        parse = this.parse(str, parse.getIndex(), "addition");
        if (parse.equals(Parser.FAIL)){
            return Parser.FAIL;
        }
        int addition_value = parse.getValue();
        parse = this.parse(str, parse.getIndex(), "opt_space");
        if (str.charAt(parse.getIndex()) != ')'){ // if expression does not match add
            parse = this.parse(str, index_before_expression, "mul_div_expression");
            if (parse.equals(Parser.FAIL)){
                return Parser.FAIL;
            }
            return new Parse(parse.getValue(), parse.getIndex() + 1);
        }
        return new Parse(addition_value, parse.getIndex() + 1);
    }
    //operand, 0 or more ( opt_space + opt_space operand)
    private Parse parse_addition_expression(String str, int index){
        //always start with an integer
        Parse parse = this.parse(str, index, "operand"); //parse out the first integer
        if (parse.equals(Parser.FAIL)){ // if not start a with integer, fail
            return Parser.FAIL;
        }
        int result = parse.getValue();
        index = parse.getIndex();

        List<Parse> parses = zero_or_more(str, index, new ArrayList<>(
                Arrays.asList("opt_space", "add_sub_operator", "opt_space", "operand")
        ));
        for (Parse p: parses){
            result = result + p.getValue();
            index = p.getIndex();
        }
        return new Parse(result, index);
    }

    //operand, 0 or more ( opt_space mul_div_operator opt_space operand)
    private Parse parse_mul_div_expression(String str, int index){
        //always start with an integer
        Parse parse = this.parse(str, index, "operand"); //parse out the first integer
        if (parse.equals(Parser.FAIL)){ // if not start a with integer, fail
            return Parser.FAIL;
        }
        int result = parse.getValue();
        index = parse.getIndex();

        List<Parse> parses = zero_or_more(str, index, new ArrayList<>(
                Arrays.asList("opt_space", "mul_div_operator", "opt_space", "operand")
        ));
        for (int i = 0; i < parses.size(); i++){
            if((index - 1) >= 1 && str.charAt(index - 1) == '*'){ // always an integer before index, no out of bound index

            }
            index = parses.get(i).getIndex();
            // zero_to_more always return 4*x parses
            // calculate only when its a operand
            if (i % 4 == 3) {
                result = result * parses.get(i).getValue();
            }
        }
        return new Parse(result, index);
    }

    // doesn't distinguish between operators - trying to fit in zero_or_more structure
    private Parse parse_add_sub_operator(String str, int index){
        if(index >= str.length()){ //empty string or index out of range
            return FAIL;
        }
        if (str.charAt(index) == '+'){
            return new Parse(0,index + 1);
        } else if (str.charAt(index) == '-'){
            return new Parse(0,index + 1);
        }
        return FAIL;
    }
    private Parse parse_mul_div_operator(String str, int index){
        if(index >= str.length()){ //empty string or index out of range
            return FAIL;
        }
        if (str.charAt(index) == '*'){
            return new Parse(0,index + 1);
        } else if (str.charAt(index) == '/'){
            return new Parse(0,index + 1);
        }
        return FAIL;
    }


    private Parse parse_integer(String str, int index){ // 1 or more integer
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
    //0 or more space
    private Parse parse_opt_space(String str, int index){
        if(index >= str.length()){ //empty string or index out of range
            return new Parse(0,index);
        }
        if (str.charAt(index) != ' '){
            return new Parse(0,index);
        }
        while (index < str.length()){
            if (str.charAt(index) != ' ') {
                break;
            }
            index++;
        }
        return new Parse(0,index);
    }

    // not using for the grammar contains it self, for example opt_space
    // return a list of parses for repetition
    // if, for one iteration, some of the terms are not valid, ignore the current iteration
    private List<Parse> zero_or_more(String str, int index, List<String> terms){
        ArrayList<Parse> parses = new ArrayList<>();
        Parse parse;
        int current_index = index; // partial index, increment when one term read in successfully

        iteration:
        while (index < str.length()){
            ArrayList<Parse> current_parses = new ArrayList<>();
            for (String term: terms){
                parse = this.parse(str, current_index, term);
                // System.out.println(parse.toString());
                if(parse.equals(Parser.FAIL)){ // ignore current iteration
                    break iteration;
                }
                current_index = parse.getIndex();
                current_parses.add(parse);
            }
            for (Parse p : current_parses){
                // System.out.println("iteration" + p.toString());
            }
            parses.addAll(current_parses);
        }
        // if zero, return a empty list
        if (parses.size() == 0){
            return new ArrayList<Parse>();
        }
        /*
        // debug
        for (Parse p : parses){
            System.out.println("iteration" + p.toString());
        }
        */
        return parses;
    }

    private Boolean reached_end(String str, int index){
        return index == str.length();
    }

    private static void test(Parser parser, String str, String term, Parse expected) {
        System.out.println(str);
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

        //opt_space test
        test(parser, "", "opt_space",new Parse(0,0));
        test(parser, " ", "opt_space",new Parse(0,1));
        test(parser, "   ", "opt_space",new Parse(0,3));

        test(parser, "3 + 4", "addition",new Parse(7,5));
        test(parser, "3  +  4", "addition",new Parse(7,7));
        //test(parser, " 3 + 4 ", "addition",new Parse(7,7)); //not in grammar
        test(parser, "(3 + 4)", "addition",new Parse(7,7));
        test(parser, "( 3+4 )", "addition",new Parse(7,7));
        test(parser, "4 +( 1+2+ 3)+ 5", "addition",new Parse(15,15));
        test(parser, "3 +(4+ (5+ 6)+9)", "addition",new Parse(27,16));

        //mul_div tests
        test(parser, "b", "mul_div_expression", Parser.FAIL);
        test(parser, "", "mul_div_expression", Parser.FAIL);
        test(parser, "3*", "mul_div_expression", new Parse(3,1));
        test(parser, "3//", "mul_div_expression", new Parse(3,1));
        test(parser, "3*4", "mul_div_expression", new Parse(12,3));
        //test(parser, "3/4", "mul_div_expression", new Parse(12,3));
        test(parser, "20*25", "mul_div_expression",new Parse(500,5));
        test(parser, "0*0", "mul_div_expression",new Parse(0,3));
        test(parser, "1*1/", "mul_div_expression",new Parse(1,3));
        test(parser, "1*1*/", "mul_div_expression",new Parse(1,3));
        test(parser, "1 *3 *(4* 5)", "mul_div_expression",new Parse(60,12));
        test(parser, "42*0", "mul_div_expression",new Parse(0,4));
        test(parser, "1 *(3 *(4* 5))", "mul_div_expression",new Parse(60,14));

    }
    public static void main(String[] args) {
        test();
    }

}