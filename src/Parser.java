import java.util.ArrayList;
import java.util.Arrays;
import java.util.List; // new ArrayList<>(Arrays.asList(meat, salad, dessert)

// index increment inside parse_integer
// current index = number of character read, and the string position for next character
// use index directly in methods, increment only when read in characters
public class Parser {

    static Parse FAIL = new Parse("Fail", -1); // a correct one with never produce -1 index
    static StatementParse STATEMENT_FAIL = new StatementParse("FAIL", -1);

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
        } else if (term.equals("add_sub_expression")){
            return this.parse_add_sub_expression(str, index);
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
    private StatementParse parse_operand(String str, int index){
        StatementParse parse = (IntegerParse) this.parse(str, index, "integer");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }
        parse = (StatementParse) this.parse(str, index,"parenthesis");
        if(!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }
        return Parser.STATEMENT_FAIL;
    }
    // ( opt_space expression opt_space )
    private StatementParse parse_parenthesis(String str, int index){
        if (index == str.length() || str.charAt(index) != '('){ // short circuit - check if empty string/index at end
            return Parser.STATEMENT_FAIL;
        }
        Parse spaces = this.parse(str, index+1, "opt_space");
        int index_before_expression = spaces.getIndex();
        StatementParse result = (StatementParse) this.parse(str, spaces.getIndex(), "add_sub_expression");
        if (result.equals(Parser.STATEMENT_FAIL)){
            return Parser.STATEMENT_FAIL;
        }
        spaces = this.parse(str, result.getIndex(), "opt_space");
        if (str.charAt(spaces.getIndex()) != ')'){ // if expression does not match add
            result = (StatementParse) this.parse(str, index_before_expression, "mul_div_expression");
            if (result.equals(Parser.STATEMENT_FAIL) || str.charAt(result.getIndex()) != ')'){
                // if mul_div is not followed by )
                return Parser.STATEMENT_FAIL;
            }
            return result;
        } else {
            result.setIndex(result.getIndex() + 1);
            return result;
        }
    }

    //mul_div_expression, 0 or more ( opt_space add_sub_operator opt_space mul_div_expression)
    private StatementParse parse_add_sub_expression(String str, int index){
        //always start with an integer
        //parse out the first integer
        StatementParse left_node = (StatementParse) this.parse(str, index, "mul_div_expression");
        if (left_node.equals(Parser.STATEMENT_FAIL)){ // if not start a with mul_div/integer, fail
            return Parser.STATEMENT_FAIL;
        }
        index = left_node.getIndex();
        StatementParse result = left_node;
        List<Parse> parses = zero_or_more(str, index, new ArrayList<>(
                Arrays.asList("opt_space", "add_sub_operator", "opt_space", "mul_div_expression")
        ));

        for (int i = 0; i < parses.size(); i++){
            // zero_to_more always return 4*x parses
            if (i % 4 == 1) { // get the operation of the current iteration
                result = (StatementParse) parses.get(i); // operator will be StatementParse
                result.getChildren().add(left_node);

                result.getChildren().add((StatementParse) parses.get(i + 2)); // add the operand as the right node
                left_node = result;
            }
        }
        return result;
    }

    //operand, 0 or more ( opt_space mul_div_operator opt_space operand)
    private StatementParse parse_mul_div_expression(String str, int index){
        //always start with an integer
        StatementParse left_node = (StatementParse) this.parse(str, index, "operand"); //parse out the first integer
        if (left_node.equals(Parser.STATEMENT_FAIL)){ // if not start a with integer, fail
            return Parser.STATEMENT_FAIL;
        }
        index = left_node.getIndex();
        StatementParse result = left_node;
        List<Parse> parses = zero_or_more(str, index, new ArrayList<>(
                Arrays.asList("opt_space", "mul_div_operator", "opt_space", "operand")
        ));

        for (int i = 0; i < parses.size(); i++){
            // zero_to_more always return 4*x parses
            if (i % 4 == 1) { // get the operation of the current iteration
                result = (StatementParse) parses.get(i); // operator will be StatementParse
                result.getChildren().add(left_node);

                result.getChildren().add((StatementParse) parses.get(i + 2)); // add the operand as the right node
                left_node = result;
            }
        }
        return result;
    }

    // doesn't distinguish between operators - trying to fit in zero_or_more structure ------ TODO FIX THIS
    private StatementParse parse_add_sub_operator(String str, int index){
        if(index >= str.length()){ //empty string or index out of range
            return STATEMENT_FAIL;
        }
        if (str.charAt(index) == '+'){
            return new StatementParse("+",index + 1);
        } else if (str.charAt(index) == '-'){
            return new StatementParse("-",index + 1);
        }
        return STATEMENT_FAIL;
    }
    private StatementParse parse_mul_div_operator(String str, int index){
        if(index >= str.length()){ //empty string or index out of range
            return STATEMENT_FAIL;
        }
        if (str.charAt(index) == '*'){
            return new StatementParse("*",index + 1);
        } else if (str.charAt(index) == '/'){
            return new StatementParse("/",index + 1);
        }
        return STATEMENT_FAIL;
    }


    private StatementParse parse_integer(String str, int index){ // 1 or more integer
        String parsed = "";
        while (index < str.length() && Character.isDigit(str.charAt(index))){
            parsed += str.charAt(index);
            index++;
        }
        if (parsed.equals("")){ //not having any integer
            return Parser.STATEMENT_FAIL;
        }
        return new IntegerParse(Integer.parseInt(parsed), index);
    }

    // 0 or more space
    // space ignored in parse tree, for keeping track of the index only
    private Parse parse_opt_space(String str, int index){
        if(index >= str.length()){ //empty string or index out of range
            return new Parse("opt_space", index);
        }
        if (str.charAt(index) != ' '){
            return new Parse("opt_space", index);
        }
        while (index < str.length()){
            if (str.charAt(index) != ' ') {
                break;
            }
            index++;
        }
        return new Parse("opt_space", index);
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
            return new ArrayList<>();
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


}