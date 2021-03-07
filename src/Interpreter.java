import java.util.Arrays;
import java.util.HashSet;

public class Interpreter {
    String output = "";
    Closure main = new Closure();

    // not include syntax error - deal during parsing
    public String execute (Parse node){
        System.out.println("s expression "+ node);
        String result = "";
        try {
            result = exec(node);
        } catch (ArithmeticException e){
            result = "runtime error: divide by zero";
        } catch (RuntimeException e){
            e.printStackTrace();
        }
        return result;
    }
    public String exec(Parse node){
        StatementParse statementNode = (StatementParse) node;
        switch (node.getName()) {
            case "FAIL":
                return "syntax error";
            case "sequence":
                return exec_sequence(statementNode);
            case "statement":
                return exec_statement(statementNode);
            case "print":
                return exec_print(statementNode);
            case "expression":
                return exec_expression(statementNode);
            case "declare":
                //exec_declare(statementNode);
            case "assign":
                //exec_assign(statementNode);
            case "varloc":
                //exec_varloc(statementNode);
            case "lookup":
                //exec_lookup(statementNode);
                break;
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
        HashSet<String> operators = new HashSet<>(){{addAll(
                Arrays.asList("+", "-", "*", "/", "integer"));
        }};
        if (operators.contains(node.getChildren().get(0).getName())){
            return exec_expression(node.getChildren().get(0)) + "\n";
        }
        return exec(node.getChildren().get(0)) + "\n";
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
            default:
                return 0;
        }
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


}
