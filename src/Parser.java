import java.util.*;

// index increment inside parse_integer
// current index = number of character read, and the string position for next character
// use index directly in methods, increment only when read in characters
public class Parser {
    static HashSet<String> keywords = new HashSet<>(){{addAll(
            Arrays.asList("print", "var", "if", "else", "while", "func",
                    "ret", "class", "int", "bool", "string"));
    }};

    static Parse FAIL = new Parse("FAIL", -1); // a correct one with never produce -1 index
    static StatementParse STATEMENT_FAIL = new StatementParse("FAIL", -1);

    // check if successfully parsed to the end
    public Parse parse(String str){
        Parse result = this.parse(str, 0, "program");
        if (result.getIndex() != str.length() || result.equals(FAIL) || result.equals(STATEMENT_FAIL)){
            System.out.println("------------------Could not parse to the end---------------------------");
            System.out.println("s-expression: " + result);
            if (result.getIndex() != -1) System.out.println("Parsed: " + str.substring(0, result.getIndex()));
            System.out.println("index: " + result.getIndex());
            System.out.println("--------------------------------------------------------------");
            return null;
        }
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
            case "if_statement":
                return this.parse_if_statement(str, index);
            case "if_else_statement":
                return this.parse_if_else_statement(str, index);
            case "while_statement":
                return this.parse_while_statement(str, index);
            case "return_statement":
                return this.parse_return_statement(str, index);
        }

        switch (term) {
            case "class":
                return this.parse_class(str, index);
            case "call_expression":
                return this.parse_call_expression(str, index);
            case "function_call":
                return this.parse_function_call(str, index);
            case "arguments":
                return this.parse_arguments(str, index);
            case "parameters":
                return this.parse_parameters(str, index);
            case "function":
                return this.parse_function(str, index);
            case "or_expression":
                return this.parse_or_expression(str, index);
            case "and_expression":
                return this.parse_and_expression(str, index);
            case "or_operator":
                return this.parse_or_operator(str, index);
            case "and_operator":
                return this.parse_and_operator(str, index);
            case "optional_not_expression":
                return this.parse_optional_not_expression(str, index);
            case "not_expression":
                return this.parse_not_expression(str, index);
            case "comp_operator":
                return this.parse_comp_operator(str, index);
            case "comp_expression":
                return this.parse_comp_expression(str, index);
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
            case ",":
                return this.parse_comma(str, index);
            default:
                throw new AssertionError("Unexpected term " + term);
        }
    }

    //opt_space ( statement opt_space )*;
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
            program.setIndex(parses.get(i).getIndex());
        }

        /*
        // if str not empty after opt_space, but not recognizing statement - syntax error
        if (str.length() != index && parses.size() == 0){
            return Parser.STATEMENT_FAIL;
        }*/

        return program;
    }
    // statements
    private StatementParse parse_statement(String str, int index){
        StatementParse parse = (StatementParse) this.parse(str, index, "declaration_statement");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }
        parse = (StatementParse) this.parse(str, index, "assignment_statement");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }
        parse = (StatementParse) this.parse(str, index, "if_else_statement");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }
        parse = (StatementParse) this.parse(str, index, "if_statement");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }
        parse = (StatementParse) this.parse(str, index, "while_statement");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }
        parse = (StatementParse) this.parse(str, index, "return_statement");
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
            if (charAt(str, index, ';')){
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
        if (!charAt(str, index, ';')){
            return STATEMENT_FAIL;
        }
        expression.setIndex(index + 1);
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
        StatementParse declare = new StatementParse("declare", assign.getIndex());
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
        if (!charAt(str, spaces.getIndex(), '=')){
            return Parser.STATEMENT_FAIL;
        }
        spaces = this.parse(str, spaces.getIndex() + 1, "opt_space");
        StatementParse expression = (StatementParse) this.parse(str, spaces.getIndex(), "expression");
        if (expression.equals(STATEMENT_FAIL)){
            return STATEMENT_FAIL;
        }
        spaces = this.parse(str, expression.getIndex(), "opt_space");
        if (!charAt(str, spaces.getIndex(), ';')){
            return STATEMENT_FAIL;
        }
        StatementParse assign = new StatementParse("assign", spaces.getIndex() + 1);
        assign.getChildren().add(location);
        assign.getChildren().add(expression);
        return assign;
    }

    // "if" opt_space "(" opt_space expression opt_space ")" opt_space "{" opt_space program opt_space "}"
    private StatementParse parse_if_statement(String str, int index){
        if (!str.startsWith("if", index)) return STATEMENT_FAIL;
        Parse spaces = this.parse(str, index + 2, "opt_space");
        index = spaces.getIndex();

        if (!charAt(str, index, '(')) return STATEMENT_FAIL;
        spaces = this.parse(str, index + 1, "opt_space");
        index = spaces.getIndex();
        StatementParse expression = (StatementParse) this.parse(str, index,"expression");
        if (expression.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;
        spaces = this.parse(str, expression.getIndex(), "opt_space");
        index = spaces.getIndex();
        if (!charAt(str, index, ')')) return STATEMENT_FAIL;
        spaces = this.parse(str, index + 1, "opt_space");
        index = spaces.getIndex();

        if (!charAt(str, index, '{')) return STATEMENT_FAIL;
        spaces = this.parse(str, index + 1, "opt_space");
        index = spaces.getIndex();
        StatementParse program = (StatementParse) this.parse(str, index, "program");
        if (program.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;
        spaces = this.parse(str, program.getIndex(), "opt_space");
        index = spaces.getIndex();
        if (!charAt(str, index, '}')) return STATEMENT_FAIL;
        index = index + 1;

        StatementParse if_statement = new StatementParse("if", index);
        if_statement.getChildren().add(expression);
        if_statement.getChildren().add(program);
        return if_statement;
    }

    // "if" opt_space "(" opt_space expression opt_space ")" opt_space "{" opt_space program opt_space "}"
    // opt_space "else" opt_space "{" opt_space program opt_space "}"
    private StatementParse parse_if_else_statement(String str, int index){
        StatementParse if_statement = this.parse_if_statement(str, index);
        if (if_statement.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;

        Parse spaces = this.parse(str, if_statement.getIndex(), "opt_space");
        index = spaces.getIndex();
        if (!str.startsWith("else", index)) return STATEMENT_FAIL;
        spaces = this.parse(str, index + 4, "opt_space");
        index = spaces.getIndex();

        if (!charAt(str, index, '{')) return STATEMENT_FAIL;
        spaces = this.parse(str, index + 1, "opt_space");
        index = spaces.getIndex();
        StatementParse program = (StatementParse) this.parse(str, index, "program");
        if (program.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;
        spaces = this.parse(str, program.getIndex(), "opt_space");
        index = spaces.getIndex();
        if (!charAt(str, index, '}')) return STATEMENT_FAIL;
        index = index + 1;

        if_statement.setName("ifelse");
        if_statement.setIndex(index);
        if_statement.getChildren().add(program);
        return if_statement;
    }

    // "while" opt_space "(" opt_space expression opt_space ")" opt_space "{" opt_space program opt_space "}"
    private StatementParse parse_while_statement(String str, int index){
        if (!str.startsWith("while", index)) return STATEMENT_FAIL;
        Parse spaces = this.parse(str, index + 5, "opt_space");
        index = spaces.getIndex();

        if (!charAt(str, index, '(')) return STATEMENT_FAIL;
        spaces = this.parse(str, index + 1, "opt_space");
        index = spaces.getIndex();
        StatementParse expression = (StatementParse) this.parse(str, index,"expression");
        if (expression.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;
        spaces = this.parse(str, expression.getIndex(), "opt_space");
        index = spaces.getIndex();
        if (!charAt(str, index, ')')) return STATEMENT_FAIL;
        spaces = this.parse(str, index + 1, "opt_space");
        index = spaces.getIndex();

        if (!charAt(str, index, '{')) return STATEMENT_FAIL;
        spaces = this.parse(str, index + 1, "opt_space");
        index = spaces.getIndex();
        StatementParse program = (StatementParse) this.parse(str, index, "program");
        if (program.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;
        spaces = this.parse(str, program.getIndex(), "opt_space");
        index = spaces.getIndex();
        if (!charAt(str, index, '}')) return STATEMENT_FAIL;
        index = index + 1;

        StatementParse while_statement = new StatementParse("while", index);
        while_statement.getChildren().add(expression);
        while_statement.getChildren().add(program);
        return while_statement;
    }

    // "ret" req_space expression opt_space ";";
    private StatementParse parse_return_statement(String str, int index){
        if (str.startsWith("ret", index)){
            index = index + 3;
            Parse parse = this.parse(str, index, "req_space");
            if (parse.equals(Parser.FAIL)) return Parser.STATEMENT_FAIL;
            index = parse.getIndex();
            StatementParse expression = (StatementParse) this.parse(str, index, "expression");
            if (expression.equals(Parser.STATEMENT_FAIL)) return Parser.STATEMENT_FAIL;
            index = expression.getIndex();
            parse = this.parse(str, index, "opt_space");
            index = parse.getIndex();
            if (charAt(str, index, ';')){
                StatementParse result = new StatementParse("return", index + 1);
                result.getChildren().add(expression);
                return result;
            }
        }
        return Parser.STATEMENT_FAIL;
    }

    //and_expression_expression, 0 or more ( opt_space or_operator opt_space and_expression)
    private StatementParse parse_or_expression(String str, int index){
        StatementParse left_node = (StatementParse) this.parse(str, index, "and_expression");
        if (left_node.equals(STATEMENT_FAIL)){
            return STATEMENT_FAIL;
        }
        index = left_node.getIndex();
        StatementParse result = left_node;
        List<Parse> parses = zero_or_more(str, index, new ArrayList<>(
                Arrays.asList("opt_space", "or_operator", "opt_space", "and_expression")
        ));
        for (int i = 0; i < parses.size(); i++){
            // zero_to_more always return 4*x parses
            if (i % 4 == 1) {
                result = (StatementParse) parses.get(i);
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

    //optional_not_expression_expression, 0 or more ( opt_space and_operator opt_space optional_not_expression)
    private StatementParse parse_and_expression(String str, int index){
        StatementParse left_node = (StatementParse) this.parse(str, index, "optional_not_expression");
        if (left_node.equals(STATEMENT_FAIL)){
            return STATEMENT_FAIL;
        }
        index = left_node.getIndex();
        StatementParse result = left_node;
        List<Parse> parses = zero_or_more(str, index, new ArrayList<>(
                Arrays.asList("opt_space", "and_operator", "opt_space", "optional_not_expression")
        ));
        for (int i = 0; i < parses.size(); i++){
            // zero_to_more always return 4*x parses
            if (i % 4 == 1) {
                result = (StatementParse) parses.get(i);
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

    private StatementParse parse_or_operator(String str, int index){
        if (str.startsWith("||", index)){
            return new StatementParse("||",index + 2);
        }
        return STATEMENT_FAIL;
    }

    private StatementParse parse_and_operator(String str, int index){
        if (str.startsWith("&&", index)){
            return new StatementParse("&&",index + 2);
        }
        return STATEMENT_FAIL;
    }

    // comp_expression or not_expression
    private StatementParse parse_optional_not_expression(String str, int index){
        StatementParse expression = (StatementParse) this.parse(str, index, "comp_expression");
        if (expression.equals(STATEMENT_FAIL)) {
            expression = (StatementParse) this.parse(str, index, "not_expression");
            if (expression.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;
            return expression;
        }
        return expression;
    }

    // "!" opt_space comp_expression
    // (! (expression))
    private StatementParse parse_not_expression(String str, int index){
        if (!charAt(str, index, '!')) return STATEMENT_FAIL;
        Parse spaces = this.parse(str, index + 1, "opt_space");
        index = spaces.getIndex();
        StatementParse expression = (StatementParse) this.parse(str, index, "comp_expression");
        if (expression.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;
        StatementParse not = new StatementParse("!", expression.getIndex());
        not.getChildren().add(expression);
        return not;
    }
    //add_subexpression, 0 or 1 ( opt_space comp_operator opt_space add_sub_expression)
    private StatementParse parse_comp_expression(String str, int index){
        StatementParse left_node = (StatementParse) this.parse(str, index, "add_sub_expression");
        // if not start a with mul_div/integer, fail
        if (left_node.equals(Parser.STATEMENT_FAIL)) return Parser.STATEMENT_FAIL;
        index = left_node.getIndex();
        StatementParse result = left_node;
        List<Parse> parses = zero_or_more(str, index, new ArrayList<>(
                Arrays.asList("opt_space", "comp_operator", "opt_space", "add_sub_expression")
        ));
        for (int i = 0; i < parses.size() && i < 4; i++){ // 0 or 1
            // zero_to_more always return 4*x parses
            if (i % 4 == 1) { // get the operation of the current iteration
                result = (StatementParse) parses.get(i);
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

    // comparison operators
    private StatementParse parse_comp_operator(String str, int index){
        if (str.startsWith("==", index)) return new StatementParse("==", index + 2);
        else if (str.startsWith("!=", index)) return new StatementParse("!=", index + 2);
        else if (str.startsWith("<=", index)) return new StatementParse("<=", index + 2);
        else if (str.startsWith(">=", index)) return new StatementParse(">=", index + 2);
        else if (str.startsWith("<", index)) return new StatementParse("<", index + 1);
        else if (str.startsWith(">", index)) return new StatementParse(">", index + 1);
        return STATEMENT_FAIL;
    }

    // operand ( opt_space function_call )*
    // (call (lookup func) (arguments))
    private Parse parse_call_expression(String str, int index){
        StatementParse operand = (StatementParse) this.parse(str, index, "operand");
        if (operand.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;

        List<Parse> parses = zero_or_more(str, operand.getIndex(), new ArrayList<>(
                Arrays.asList("opt_space", "function_call")
        ));
        for (int i = 0; i < parses.size(); i++){
            if (i % 2 == 1) {
                StatementParse function = (StatementParse) parses.get(i);
                function.getChildren().add(0, operand);
                operand = function;
            }
        }
        return operand;
    }

    // "(" opt_space arguments opt_space ")"
    // (call (arguments)) - (lookup func) should be before arguments
    private Parse parse_function_call(String str, int index){
        if (!charAt(str, index, '(')) return STATEMENT_FAIL;
        Parse spaces = this.parse(str, index + 1, "opt_space");
        index = spaces.getIndex();
        StatementParse arguments = (StatementParse) this.parse(str, index,"arguments");
        if (arguments.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;
        spaces = this.parse(str, arguments.getIndex(), "opt_space");
        index = spaces.getIndex();
        if (!charAt(str, index, ')')) return STATEMENT_FAIL;

        StatementParse call = new StatementParse("call", index + 1);
        call.getChildren().add(arguments);
        return call;
    }

    // 0 or 1 ( expression opt_space ( "," opt_space expression opt_space )* )
    private Parse parse_arguments(String str, int index){
        StatementParse arguments = new StatementParse("arguments", index);
        StatementParse expression = (StatementParse) this.parse(str, index, "expression");
        if (expression.equals(STATEMENT_FAIL)) return arguments;
        arguments.getChildren().add(expression);
        arguments.setIndex(expression.getIndex());

        Parse spaces = this.parse(str, expression.getIndex(), "opt_space");
        index = spaces.getIndex();

        List<Parse> parses = zero_or_more(str, index, new ArrayList<>(
                Arrays.asList(",", "opt_space", "expression", "opt_space")
        ));
        for (int i = 0; i < parses.size(); i++){
            // zero_to_more always return 4*x parses
            if (i % 4 == 2) { // get the operation of the current iteration
                expression = (StatementParse) parses.get(i);
                arguments.getChildren().add(expression);
            }
        }
        arguments.setIndex(expression.getIndex());
        return arguments;
    }

    // "func" opt_space "(" opt_space parameters opt_space ")" opt_space "{" opt_space program opt_space "}"
    private Parse parse_function(String str, int index){
        if (!str.startsWith("func", index)) return STATEMENT_FAIL;
        Parse spaces = this.parse(str, index + 4, "opt_space");
        index = spaces.getIndex();

        if (!charAt(str, index, '(')) return STATEMENT_FAIL;
        spaces = this.parse(str, index + 1, "opt_space");
        index = spaces.getIndex();
        StatementParse parameters = (StatementParse) this.parse(str, index,"parameters");
        if (parameters.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;
        spaces = this.parse(str, parameters.getIndex(), "opt_space");
        index = spaces.getIndex();
        if (!charAt(str, index, ')')) return STATEMENT_FAIL;
        spaces = this.parse(str, index + 1, "opt_space");
        index = spaces.getIndex();

        if (!charAt(str, index, '{')) return STATEMENT_FAIL;
        spaces = this.parse(str, index + 1, "opt_space");
        index = spaces.getIndex();
        StatementParse program = (StatementParse) this.parse(str, index, "program");
        if (program.equals(STATEMENT_FAIL)) return STATEMENT_FAIL;
        spaces = this.parse(str, program.getIndex(), "opt_space");
        index = spaces.getIndex();
        if (!charAt(str, index, '}')) return STATEMENT_FAIL;
        index = index + 1;

        StatementParse function = new StatementParse("function", index);
        function.getChildren().add(parameters);
        function.getChildren().add(program);
        return function;
    }

    // 0 or 1 ( identifier opt_space ( "," opt_space identifier opt_space )* )
    // (parameter var) - identifier does not have lookup
    private Parse parse_parameters(String str, int index){
        StatementParse parameter = new StatementParse("parameters", index);
        StatementParse identifier = (StatementParse) this.parse(str, index, "identifier");
        if (identifier.equals(STATEMENT_FAIL)) return parameter;
        parameter.getChildren().add(identifier.getChildren().get(0));
        parameter.setIndex(identifier.getIndex());

        Parse spaces = this.parse(str, identifier.getIndex(), "opt_space");
        index = spaces.getIndex();

        List<Parse> parses = zero_or_more(str, index, new ArrayList<>(
                Arrays.asList(",", "opt_space", "identifier", "opt_space")
        ));
        for (int i = 0; i < parses.size(); i++){
            // zero_to_more always return 4*x parses
            if (i % 4 == 2) { // get the operation of the current iteration
                identifier = (StatementParse) parses.get(i);
                parameter.getChildren().add(identifier.getChildren().get(0));
            }
        }
        parameter.setIndex(identifier.getIndex());
        return parameter;
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
        if (keywords.contains(var_name)) return Parser.STATEMENT_FAIL;
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
        StatementParse result = (StatementParse) this.parse(str, index, "or_expression");
        if (result.equals(Parser.STATEMENT_FAIL)){
            return Parser.STATEMENT_FAIL;
        }
        return result;
    }

    //class | parenthesized_expression | function | identifier | integer
    private StatementParse parse_operand(String str, int index){
        StatementParse parse = (StatementParse) this.parse(str, index,"class");
        if(!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }

        parse = (StatementParse) this.parse(str, index,"parenthesis");
        if(!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }

        parse = (StatementParse) this.parse(str, index, "function");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }

        parse = (StatementParse) this.parse(str, index, "identifier");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }

        parse = (StatementParse) this.parse(str, index, "integer");
        if (!parse.equals(Parser.STATEMENT_FAIL)){
            return parse;
        }
        return Parser.STATEMENT_FAIL;
    }

    // "class" opt_space "{" ( opt_space declaration_statement )* opt_space "}"
    private StatementParse parse_class(String str, int index){
        if (!str.startsWith("class", index)) return STATEMENT_FAIL;
        Parse spaces = this.parse(str, index + 5, "opt_space");
        index = spaces.getIndex();

        if (!charAt(str, index, '{')) return STATEMENT_FAIL;
        StatementParse class_parse = new StatementParse("class", index + 1);
        List<Parse> parses = zero_or_more(str, index + 1, new ArrayList<>(
                Arrays.asList("opt_space", "declaration_statement")
        ));
        for (int i = 0; i < parses.size(); i++){
            if (i % 2 == 1) {
                StatementParse declare = (StatementParse) parses.get(i);
                class_parse.getChildren().add(declare);
                class_parse.setIndex(declare.getIndex());
            }
        }

        spaces = this.parse(str, class_parse.getIndex(), "opt_space");
        index = spaces.getIndex();
        if (!charAt(str, index, '}')) return STATEMENT_FAIL;
        class_parse.setIndex(index + 1);

        return class_parse;
    }

    // ( opt_space expression opt_space )
    private StatementParse parse_parenthesis(String str, int index){
        if (!charAt(str, index, '(')){ // short circuit - check if empty string/index at end
            return Parser.STATEMENT_FAIL;
        }
        Parse spaces = this.parse(str, index+1, "opt_space");
        StatementParse result = (StatementParse) this.parse(str, spaces.getIndex(), "expression");
        if (result.equals(Parser.STATEMENT_FAIL)){
            return Parser.STATEMENT_FAIL;
        }
        spaces = this.parse(str, result.getIndex(), "opt_space");
        if (!charAt(str, spaces.getIndex(), ')')){ // if expression does not match add
            return Parser.STATEMENT_FAIL;
        } else {
            result.setIndex(spaces.getIndex() + 1);
        }
        return result;
    }

    //mul_div_expression, 0 or more ( opt_space add_sub_operator opt_space mul_div_expression)
    private StatementParse parse_add_sub_expression(String str, int index){
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
        return result;
    }

    //call_expression, 0 or more ( opt_space mul_div_operator opt_space call_expression)
    private StatementParse parse_mul_div_expression(String str, int index){
        //always start with an integer
        StatementParse left_node = (StatementParse) this.parse(str, index, "call_expression");
        if (left_node.equals(Parser.STATEMENT_FAIL)){
            return Parser.STATEMENT_FAIL;
        }
        index = left_node.getIndex();
        StatementParse result = left_node;
        List<Parse> parses = zero_or_more(str, index, new ArrayList<>(
                Arrays.asList("opt_space", "mul_div_operator", "opt_space", "call_expression")
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
        if (charAt(str, index, '+')) return new StatementParse("+", index + 1);
        else if (charAt(str, index, '-')) return new StatementParse("-", index + 1);
        return STATEMENT_FAIL;
    }
    private StatementParse parse_mul_div_operator(String str, int index){
        if (charAt(str, index, '*')){
            return new StatementParse("*",index + 1);
        } else if (charAt(str, index, '/')){
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
        if (charAt(str, index, ' ') || charAt(str, index, '\t') ){
            return new Parse("blank", index + 1);
        }
        if (charAt(str, index, '\n')){
            return new Parse("newline", index + 1);
        }
        return Parser.FAIL;
    }

    // "#" ( PRINT )* NEWLINE
    // if at end of the string, don't need NEWLINE
    // ignored in parse tree, for keeping track of the index only
    private Parse parse_comment(String str, int index){
        //empty string or index out of range
        if (!charAt(str, index, '#')){
            return Parser.FAIL;
        }
        while (index < str.length()){
            if (charAt(str, index, '\n')) {
                index++;
                return new Parse("comment", index);
            }
            index++;
        }
        // if reached the end of the string, and no \n
        if (index == str.length()){
            return new Parse("comment", index);
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

        iteration:
        while (index < str.length()){
            ArrayList<Parse> current_parses = new ArrayList<>();
            for (String term: terms){
                parse = this.parse(str, current_index, term);
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
        return parses;
    }

    // helper parse
    // match to ","
    private Parse parse_comma(String str, int index){
        if (charAt(str, index, ',')){
            return new Parse("comma", index + 1);
        }
        return FAIL;
    }

    // check if index out of bound, return false, to avoid exception
    private Boolean charAt(String str, int index, char x){
        return index < str.length() && str.charAt(index) == x; // short circuit
    }
    private Boolean reached_end(String str, int index){
        return index == str.length();
    }


}