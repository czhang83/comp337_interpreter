import java.util.Arrays;
import java.util.HashSet;

public class Interpreter {
    // operators for print to eval
    HashSet<String> operators = new HashSet<>(){{addAll(
            Arrays.asList("+", "-", "*", "/", "integer", "||", "&&",
                    "!", "==", "!=", "<=", ">=", "<", ">"));
    }};
    Closure mainClosure;
    Closure currentClosure;

    // not include syntax error - deal during parsing
    public String execute (Parse node){
        mainClosure = new Closure();
        currentClosure = mainClosure;

        System.out.println("s expression "+ node);
        String result = "";
        try {
            result = exec(node);
        } catch (ArithmeticException e){
            result = "runtime error: divide by zero";
        } catch (VariableAlreadyDefined e){
            result = "runtime error: variable already defined";
        } catch (UndefinedVariable e){
            result = "runtime error: undefined variable";
        } catch (RuntimeException e){
            e.printStackTrace();
        }
        return result;
    }
    public String exec(Parse node){
        if (node == null){
            return "syntax error";
        }
        StatementParse statementNode = (StatementParse) node;
        switch (node.getName()) {
            case "sequence":
                return exec_sequence(statementNode);
            case "statement":
                return exec_statement(statementNode);
            case "print":
                return exec_print(statementNode);
            case "expression":
                return exec_expression(statementNode);
            case "declare":
                exec_declare(statementNode);
                break;
            case "assign":
                exec_assign(statementNode);
                break;
            case "if":
                return exec_if(statementNode);
            case "ifelse":
                return exec_ifelse(statementNode);
            case "while":
                return exec_while(statementNode);
            case "lookup":
                return exec_lookup(statementNode);
        }
        return "";
    }

    private String exec_sequence(StatementParse node){
        String result = "";
        for (int i = 0; i < node.getChildren().size(); i++){
            result = result.concat(exec(node.getChildren().get(i)));
        }
        return result;
    }

    private String exec_statement(StatementParse node){
        return exec(node.getChildren().get(0));
    }

    private String exec_expression(StatementParse node){
        return eval(node).toString();
    }

    private String exec_print(StatementParse node){
        if (operators.contains(node.getChildren().get(0).getName())){
            return exec_expression(node.getChildren().get(0)) + "\n";
        }
        return exec(node.getChildren().get(0)) + "\n";
    }

    // (declare name value)
    // value is optional
    private void exec_declare(StatementParse node){
        String variableName = node.getChildren().get(0).getName();

        // if value is included
        if (node.getChildren().size() == 2){
            currentClosure.declare(variableName, eval(node.getChildren().get(1)));
        } else {
            currentClosure.declare(variableName);
        }
    }

    private String exec_lookup(StatementParse node){
        Value value = currentClosure.lookup(node.getChildren().get(0).getName());
        if (value instanceof IntegerValue){
            return String.valueOf(((IntegerValue) value).getValue());
        }
        return null;
    }

    private void exec_assign(StatementParse node){
        String name = node.getChildren().get(0).getChildren().get(0).getName();
        currentClosure.assign(name, eval(node.getChildren().get(1)));
    }

    private String exec_if(StatementParse node){
        String print = "";
        // if the condition is true
        // create a child closure, set it as the currentClosure
        if (isTrue(eval(node.getChildren().get(0)))){
            currentClosure = new Closure(currentClosure);
            print = exec(node.getChildren().get(1));

            // change the closure back
            currentClosure = currentClosure.getParent();
        }
        return print;
    }

    private String exec_ifelse(StatementParse node){
        String print = "";
        // if the condition is true
        // create a child closure, set it as the currentClosure
        if (isTrue(eval(node.getChildren().get(0)))){
            currentClosure = new Closure(currentClosure);
            print = exec(node.getChildren().get(1));

            // change the closure back
            currentClosure = currentClosure.getParent();
        } else {
            currentClosure = new Closure(currentClosure);
            print = exec(node.getChildren().get(2));

            // change the closure back
            currentClosure = currentClosure.getParent();
        }
        return print;
    }

    private String exec_while(StatementParse node){
        String print = "";
        // if the condition is true
        // create a child closure, set it as the currentClosure
        while (isTrue(eval(node.getChildren().get(0)))){
            currentClosure = new Closure(currentClosure);
            print = print.concat(exec(node.getChildren().get(1)));

            // change the closure back
            currentClosure = currentClosure.getParent();
        }
        return print;
    }

    public Integer eval(StatementParse node){
        switch (node.getName()) {
            case "integer":
                return ((IntegerParse) node).getValue();
            case "+":
                return eval_add(node);
            case "-":
                return eval_sub(node);
            case "*":
                return eval_mul(node);
            case "/":
                return eval_div(node);
            case "==":
                return eval_eq(node);
            case "!=":
                return eval_not_eq(node);
            case "<=":
                return eval_leq(node);
            case ">=":
                return eval_geq(node);
            case "<":
                return eval_less(node);
            case ">":
                return eval_greater(node);
            case "!":
                return eval_neg(node);
            case "||":
                return eval_or(node);
            case "&&":
                return eval_and(node);
            case "lookup":
                return eval_lookup(node);
            default:
                return 0;
        }
    }

    private Integer eval_eq(StatementParse node){
        if (eval(node.getChildren().get(0)).equals(eval(node.getChildren().get(1)))) return 1;
        return 0;
    }
    private Integer eval_not_eq(StatementParse node){
        if (!eval(node.getChildren().get(0)).equals(eval(node.getChildren().get(1)))) return 1;
        return 0;
    }
    private Integer eval_leq(StatementParse node){
        if (eval(node.getChildren().get(0)) <= eval(node.getChildren().get(1))) return 1;
        return 0;
    }
    private Integer eval_geq(StatementParse node){
        if (eval(node.getChildren().get(0)) >= eval(node.getChildren().get(1))) return 1;
        return 0;
    }
    private Integer eval_less(StatementParse node){
        if (eval(node.getChildren().get(0)) < eval(node.getChildren().get(1))) return 1;
        return 0;
    }
    private Integer eval_greater(StatementParse node){
        if (eval(node.getChildren().get(0)) > eval(node.getChildren().get(1))) return 1;
        return 0;
    }
    private Integer eval_neg(StatementParse node){
        if (isTrue(eval(node.getChildren().get(0)))) return 0;
        return 1;
    }
    private Integer eval_or(StatementParse node){
        if (isTrue(eval(node.getChildren().get(0))) || isTrue(eval(node.getChildren().get(1)))) return 1;
        return 0;
    }
    private Integer eval_and(StatementParse node){
        if (isTrue(eval(node.getChildren().get(0))) && isTrue(eval(node.getChildren().get(1)))) return 1;
        return 0;
    }

    private Integer eval_lookup(StatementParse node){
         Value value = currentClosure.lookup(node.getChildren().get(0).getName());
         if (value instanceof IntegerValue){
             return ((IntegerValue) value).getValue();
         }
         return null;
    }

    private Integer eval_add(StatementParse node){
        int result = 0;
        for (StatementParse value: node.getChildren()){
            if (value.getName().equals("integer")){
                result += ((IntegerParse) value).getValue();
            } else {
                result += eval(value);
            }
        }
        return result;
    }

    private Integer eval_sub(StatementParse node){
        int result = 0;
        for (int i = 0; i < node.getChildren().size(); i++){ // binary tree
            StatementParse value = node.getChildren().get(i);
            if (i == 0){
                if (value.getName().equals("integer")){
                    result = ((IntegerParse) value).getValue();
                } else {
                    result = eval(value);
                }
            }
            if (i == 1){
                if (value.getName().equals("integer")){
                    result -= ((IntegerParse) value).getValue();
                } else {
                    result -= eval(value);
                }
            }
        }
        return result;
    }

    private Integer eval_mul(StatementParse node){
        int result = 1;
        for (StatementParse value: node.getChildren()){
            if (value.getName().equals("integer")){
                result *= ((IntegerParse) value).getValue();
            } else {
                result *= eval(value);
            }
        }
        return result;
    }

    private Integer eval_div(StatementParse node){
        int result = 0;
        for (int i = 0; i < node.getChildren().size(); i++){ // binary tree
            StatementParse value = node.getChildren().get(i);
            if (i == 0){
                if (value.getName().equals("integer")){
                    result = ((IntegerParse) value).getValue();
                } else {
                    result = eval(value);
                }
            }
            if (i == 1){
                if (value.getName().equals("integer")){
                    result = result / ((IntegerParse) value).getValue();
                } else {
                    result = result / eval(value);
                }
            }
        }
        return result;
    }

    // 0 - False, other values - True
    private Boolean isTrue(int value){
        return value != 0;
    }


}

class VariableAlreadyDefined extends RuntimeException{
    VariableAlreadyDefined(){
        super();
    }

    VariableAlreadyDefined(String s){
        super(s);
    }
}

class UndefinedVariable extends RuntimeException{
    UndefinedVariable(){
        super();
    }

    UndefinedVariable(String s){
        super(s);
    }
}