public class Interpreter {
    String output = "";

    // not include syntax error - deal during parsing
    public void execute (Parse node){
        try {
            exec(node);
        } catch (RuntimeException e){
            //print the error
        }
    }
    public void exec(Parse node){
        eval((StatementParse) node);
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
        } else {
            return 0;
        }
    }

    private void exec_print(Parse node){
        // print(eval(node.children[0];
    }
    //exec_sequence
    //exec_if
    //exec_while
    private void exec_x(){

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
