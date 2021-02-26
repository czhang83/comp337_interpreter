public class Interpreter {
    String output = "";

    // not include syntax error - deal during parsing
    public String execute (Parse node){
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
        if (node.getName().equals("FAIL")){
            return "syntax error";
        }
        if (node.getName().equals("sequence")){
            return exec_sequence(statementNode);
        }
        if (node.getName().equals("statement")){
            return exec_statement(statementNode);
        }
        if (node.getName().equals("print")){
            return exec_print(statementNode);
        }
        if (node.getName().equals("expression")){
            exec_expression(statementNode);
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

    private Integer exec_expression(StatementParse node){
        return eval(node.getChildren().get(0));
    }

    private String exec_print(StatementParse node){
        if ((node.getChildren().get(0).getName().equals("expression"))){
            return exec_expression(node.getChildren().get(0)).toString() + "\n";
        }
        return exec(node.getChildren().get(0)) + "\n";
    }


    public Integer eval(StatementParse node){
        if (node.getName().equals("integer")){
            return ((IntegerParse) node).getValue();
        } else if (node.getName().equals("+")){
            return eval_add(node);
        } else if (node.getName().equals("-")){
            return eval_sub(node);
        } else if (node.getName().equals("*")){
            return eval_mul(node);
        } else if (node.getName().equals("/")){
            return eval_div(node);
        } else if (node.getName().equals("expression")){
            return exec_expression(node);
        }else {
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
