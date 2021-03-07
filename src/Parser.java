import javax.swing.plaf.nimbus.State;
import java.sql.Statement;
import java.util.*;

// index increment inside parse_integer
// current index = number of character read, and the string position for next character
// use index directly in methods, increment only when read in characters
public class Parser {
    static HashSet<String> keywords = new HashSet<>(){{addAll(
            Arrays.asList("print", "var", "if", "else", "while", "func",
                    "ret", "class", "int", "bool", "string"));
    }};

    static Parse FAIL = new Parse("Fail", -1); // a correct one with never produce -1 index
    static StatementParse STATEMENT_FAIL = new StatementParse("FAIL", -1);

    public Parse parse(String str){
        return this.parse(str, 0, "program");
    }
    // wrapper
    public Parse parse(String str, String term){
        return this.parse(str, 0, term);
    }
    // redirect parse to other corresponding parse methods
    private Parse parse(String str, int index, String term ){
        if(index > str.length()){ // allow empty string/check space at end - check empty string in parse functions
            return Parser.FAIL;
        }
        switch (term) {
            case "program":
                return this.parse_program(str, index);
            case "statement":
                return this.parse_statement(str, index);
            case "print_statement":
                return this.parse_print_statement(str, index);
            case "expression_statement":
                return this.parse_expression_statement(str,index);
            case "assignment_statement":
                return this.parse_assignment_statement(str, index);
            case "declaration_statement":
                return this.parse_declaration_statement(str, index);

        }

        switch (term) {
            case "location":
                return this.parse_location(str, index);
            case "identifier":
                return this.parse_identifier(str, index);
            case "integer":
                return this.parse_integer(str, index);
            case "expression":
                return this.parse_expression(str, index);
            case "add_sub_expression":
                return this.parse_add_sub_expression(str, index);
            case "operand":
                return this.parse_operand(str, index);
            case "parenthesis":
                return this.parse_parenthesis(str, index);
            case "opt_space":  //opt_space can take empty string (index = str.length())
                return this.parse_opt_space(str, index);
            case "req_space":
                return this.parse_req_space(str, index);
            case "add_sub_operator":
                return this.parse_add_sub_operator(str, index);
            case "mul_div_operator":
                return this.parse_mul_div_operator(str, index);
            case "mul_div_expression":
                return this.parse_mul_div_expression(str, index);
            case "space":
                return this.parse_space(str, index);
            case "comment":
                return this.parse_comment(str, index);
            default:
                throw new AssertionError("Unexpected term " + term);
        }
    }

    //opt_space ( statement opt_space )*;
    // TODO test
    private StatementParse parse_program(String str, int index){
        Parse parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();

        List<Parse> parses = zero_or_more(str, index, new ArrayList<>(
                Arrays.asList("statement", "opt_space")
        ));

        StatementParse program = new StatementParse("sequence", index);
        for (int i = 0; i < parses.size(); i++){
            // zero_to_more always return 4*x parses
            if (i % 2 == 0) { // get the operation of the current iteration
                StatementParse result = (StatementParse) parses.get(i); // operator will be StatementParse
                program.getChildren().add(result);
            }
        }

        // if str not empty after opt_space, but not recognizing statement - syntax error
        if (str.length() != index && parses.size() == 0){
            return Parser.STATEMENT_FAIL;
        }

        return program;
    }
    // print_state or expression
    // TODO test
    private StatementParse parse_statement(String str, int index){
        StatementParse parse = (StatementParse) this.parse(str, index, "declaration_statement");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }
        parse = (StatementParse) this.parse(str, index, "assignment_statement");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }
        parse = (StatementParse) this.parse(str, index, "print_statement");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }
        parse = (StatementParse) this.parse(str, index, "expression_statement");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }
        return STATEMENT_FAIL;
    }
    // "print" req_space expression opt_space ";";
    // TODO test
    private StatementParse parse_print_statement(String str, int index){
        if (str.startsWith("print", index)){
            index = index + 5;
            Parse parse = this.parse(str, index, "req_space");
            if (parse.equals(Parser.FAIL)){
                return Parser.STATEMENT_FAIL;
            }
            index = parse.getIndex();
            StatementParse expression = (StatementParse) this.parse(str, index, "expression");
            if (expression.equals(Parser.STATEMENT_FAIL)){
                return Parser.STATEMENT_FAIL;
            }
            index = expression.getIndex();
            parse = this.parse(str, index, "opt_space");
            index = parse.getIndex();
            if (str.charAt(index) == ';'){
                StatementParse result = new StatementParse("print", index + 1);
                result.getChildren().add(expression);
                return result;
            }
        }
        return Parser.STATEMENT_FAIL;
    }

    // expression_statement = expression opt_space ";";
    // return node name expression
    private StatementParse parse_expression_statement(String str, int index){
        StatementParse expression = (StatementParse) this.parse(str, index, "expression");
        if (expression.equals(Parser.STATEMENT_FAIL)){
            return Parser.STATEMENT_FAIL;
        }
        index = expression.getIndex();
        Parse parse = this.parse(str, index, "opt_space");
        index = parse.getIndex();
        expression.setIndex(index);
        return expression;
    }

    //"var" req_space assignment_statement;
    // (assign var_name (+ expression))
    private StatementParse parse_declaration_statement(String str, int index){
        if (!str.startsWith("var", index)){
            return STATEMENT_FAIL;
        }
        index = index + 3;
        Parse space = this.parse(str, index, "req_space");
        if (space.equals(STATEMENT_FAIL)){
            return STATEMENT_FAIL;
        }
        StatementParse assign = (StatementParse) this.parse(str, space.getIndex(), "assignment_statement");
        if (assign.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;
        StatementParse declare = new StatementParse("declare", assign.getIndex() + 1);
        declare.getChildren().add(assign.getChildren().get(0).getChildren().get(0));
        declare.getChildren().add(assign.getChildren().get(1));
        return declare;
    }
    // location opt_space "=" opt_space expression opt_space ";";
    // assign node, children: location, expression
    private StatementParse parse_assignment_statement(String str, int index){
        StatementParse location = (StatementParse) this.parse(str, index, "location");
        if (location.equals(Parser.STATEMENT_FAIL)){
            return Parser.STATEMENT_FAIL;
        }
        index = location.getIndex();
        Parse spaces = this.parse(str, index, "opt_space");
        if (str.charAt(spaces.getIndex()) != '='){
            return Parser.STATEMENT_FAIL;
        }
        spaces = this.parse(str, spaces.getIndex() + 1, "opt_space");
        StatementParse expression = (StatementParse) this.parse(str, spaces.getIndex(), "expression");
        if (expression.equals(STATEMENT_FAIL)){
            return STATEMENT_FAIL;
        }
        spaces = this.parse(str, expression.getIndex(), "opt_space");
        if (str.charAt(spaces.getIndex()) != ';'){
            return STATEMENT_FAIL;
        }
        StatementParse assign = new StatementParse("assign", spaces.getIndex() + 1);
        assign.getChildren().add(location);
        assign.getChildren().add(expression);
        return assign;
    }

    /**
     * identifier               = identifier_first_char ( identifier_char )*;
     * identifier_first_char    = ALPHA
     *                          | "_";
     * identifier_char          = ALNUM
     *                          | "_";
     * var name can not be keywords
     */
    // variable default to (lookup var_name)
    private StatementParse parse_identifier(String str, int index){
        // if var does not start with ALPHA or _, fail
        if (index == str.length() || (!Character.isLetter(str.charAt(index)) && str.charAt(index) != '_')){
            return Parser.STATEMENT_FAIL;
        }
        int start_index = index;
        index++;
        while (Character.isLetter(str.charAt(index)) ||
                str.charAt(index) == '_' || Character.isDigit(str.charAt(index))){
            index++;
        }
        String var_name = str.substring(start_index, index);
        if (keywords.contains(var_name)){
            return Parser.STATEMENT_FAIL;
        }
        IdentifierParse variable = new IdentifierParse(var_name, index);
        StatementParse lookup = new StatementParse("lookup", index);
        lookup.getChildren().add(variable);
        return lookup;
    }

    // identifier
    // return location node, identifier as its child
    private StatementParse parse_location(String str, int index){
        StatementParse lookup = (StatementParse) this.parse(str, index, "identifier");
        if (lookup.equals(Parser.STATEMENT_FAIL)){
            return Parser.STATEMENT_FAIL;
        }
        StatementParse location = new StatementParse("varloc", lookup.getIndex());
        location.getChildren().add(lookup.getChildren().get(0));
        return location;
    }

    private StatementParse parse_expression(String str, int index){
        StatementParse result = this.parse_add_sub_expression(str, index);
        if (result.equals(Parser.STATEMENT_FAIL)){
            return Parser.STATEMENT_FAIL;
        }
        return result;
    }

    //either integer or parenthesis
    private StatementParse parse_operand(String str, int index){
        //System.out.println("start parsing paren");
        StatementParse parse = (StatementParse) this.parse(str, index,"parenthesis");
        // System.out.println("parsing paren: " +parse);
        //System.out.println(!parse.equals(Parser.STATEMENT_FAIL));
        if(!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }

        parse = (StatementParse) this.parse(str, index, "identifier");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }

        parse = (StatementParse) this.parse(str, index, "integer");
        //System.out.println("parsing integer: " + parse);
        //System.out.println(!parse.equals(Parser.STATEMENT_FAIL));
        if (!parse.equals(Parser.STATEMENT_FAIL)){
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
        StatementParse result = (StatementParse) this.parse(str, spaces.getIndex(), "expression");
        if (result.equals(Parser.STATEMENT_FAIL)){
            return Parser.STATEMENT_FAIL;
        }
        spaces = this.parse(str, result.getIndex(), "opt_space");
        if (str.charAt(spaces.getIndex()) != ')'){ // if expression does not match add
            return Parser.STATEMENT_FAIL;
        } else {
            result.setIndex(spaces.getIndex() + 1);
        }
        return result;
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

                StatementParse right_node = (StatementParse) parses.get(i + 2);
                index = right_node.getIndex();
                result.getChildren().add(right_node); // add the operand as the right node
                result.setIndex(index); // set the index of the right node as the index for the parent node
                left_node = result;
            }
        }
        //StatementParse expression = new StatementParse("expression", index);
        //expression.getChildren().add(result);
        //return expression;
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

                StatementParse right_node = (StatementParse) parses.get(i + 2);
                index = right_node.getIndex();
                result.getChildren().add(right_node); // add the operand as the right node
                result.setIndex(index); // set the index of the right node as the index for the parent node
                left_node = result;
            }
        }
        return result;
    }

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
        StringBuilder parsed = new StringBuilder();
        while (index < str.length() && Character.isDigit(str.charAt(index))){
            parsed.append(str.charAt(index));
            index++;
        }
        if (parsed.toString().equals("")){ //not having any integer
            return Parser.STATEMENT_FAIL;
        }
        return new IntegerParse(Integer.parseInt(parsed.toString()), index);
    }

    // 0 or more space
    // space ignored in parse tree, for keeping track of the index only
    private Parse parse_opt_space(String str, int index){
        if(index >= str.length()){ //empty string or index out of range
            return new Parse("opt_space", index);
        }
        while (index < str.length()){
            Parse parse = this.parse(str, index, "space");
            if (parse.equals(Parser.FAIL)){
                break;
            }
            index = parse.getIndex();
        }
        return new Parse("opt_space", index);
    }

    // 1 or more space
    // space ignored in parse tree, for keeping track of the index only
    private Parse parse_req_space(String str, int index){
        if(index >= str.length()){ //empty string or index out of range
            return Parser.FAIL;
        }
        Parse parse = this.parse(str, index, "space");
        if (parse.equals(Parser.FAIL)){
            return Parser.FAIL;
        }
        index = parse.getIndex();
        while (index < str.length()){
            parse = this.parse(str, index, "space");
            if (parse.equals(Parser.FAIL)){
                break;
            }
            index = parse.getIndex();
        }
        return new Parse("req_space", index);
    }

    // comment | BLANK | NEWLINE
    // ignored in parse tree, for keeping track of the index only
    private Parse parse_space(String str, int index){
        if(index >= str.length()){ //empty string or index out of range
            return Parser.FAIL;
        }
        Parse parse = this.parse(str, index, "comment");
        if (!parse.equals(Parser.FAIL)){
            return parse;
        }
        if (str.charAt(index) == ' '){
            return new Parse("blank", index + 1);
        }
        if (str.charAt(index) == '\n'){
            return new Parse("newline", index + 1);
        }
        return Parser.FAIL;
    }

    // "#" ( PRINT )* NEWLINE
    // ignored in parse tree, for keeping track of the index only
    private Parse parse_comment(String str, int index){
        if(index >= str.length()){ //empty string or index out of range
            return Parser.FAIL;
        }
        if (str.charAt(index) != '#'){
            return Parser.FAIL;
        }
        while (index < str.length()){
            if (str.charAt(index) == '\n') {
                index++;
                return new Parse("comment", index);
            }
            index++;
        }
        // no newline exist
        return Parser.FAIL;
    }

    // not using for the grammar contains it self, for example opt_space
    // return a list of parses for repetition
    // if, for one iteration, some of the terms are not valid, ignore the current iteration
    private List<Parse> zero_or_more(String str, int index, List<String> terms){
        ArrayList<Parse> parses = new ArrayList<>();
        Parse parse;
        int current_index = index; // partial index, increment when one term read in successfully

        //System.out.println("---------------------------------------------------------------------------");
        iteration:
        while (index < str.length()){
            ArrayList<Parse> current_parses = new ArrayList<>();
            for (String term: terms){
                parse = this.parse(str, current_index, term);
                //System.out.println(str.substring(0, current_index));
                //System.out.println(parse.getName());
                if(parse.equals(Parser.FAIL) || parse.equals(Parser.STATEMENT_FAIL)){ // ignore current iteration
                    break iteration;
                }
                current_index = parse.getIndex();
                current_parses.add(parse);
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