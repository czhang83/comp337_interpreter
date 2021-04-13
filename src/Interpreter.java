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

    String consoleOutput = "";

    // not include syntax error - deal during parsing
    public String execute (Parse node){
        mainClosure = new Closure();
        currentClosure = mainClosure;

        System.out.println("s expression "+ node);
        consoleOutput = "";
        try {
            exec(node);
        } catch (ArithmeticException e){
            consoleOutput = "runtime error: divide by zero";
        } catch (VariableAlreadyDefined e){
            consoleOutput = "runtime error: variable already defined";
        } catch (UndefinedVariable e){
            consoleOutput = "runtime error: undefined variable";
        } catch (DuplicateParam e){
            consoleOutput = "runtime error: duplicate parameter";
        }

        catch (CallingNonFunction e){
            consoleOutput = "runtime error: calling a non-function";
        } catch (ArgumentMismatch e){
            consoleOutput = "runtime error: argument mismatch";
        }
        catch (RuntimeException e){
            e.printStackTrace();
        }
        return consoleOutput;
    }

    // return value for functions only
    public Value exec(Parse node){
        if (node == null){
            consoleOutput = "syntax error";
        }
        StatementParse statementNode = (StatementParse) node;
        switch (node.getName()) {
            case "sequence":
                return exec_sequence(statementNode);
            case "statement":
                exec_statement(statementNode); break;
            case "print":
                exec_print(statementNode); break;
            case "expression":
                exec_expression(statementNode); break;
            case "declare":
                exec_declare(statementNode); break;
            case "assign":
                exec_assign(statementNode); break;
            case "if":
                exec_if(statementNode); break;
            case "ifelse":
                exec_ifelse(statementNode); break;
            case "while":
                exec_while(statementNode); break;
            case "call":
                return exec_call(statementNode);
            case "lookup": // only when not returning anything
                exec_lookup(statementNode); break;
            case "return":
                return exec_return(statementNode);
        }
        return null;
    }

    public Value exec_get_value(StatementParse node) {
        switch (node.getName()) {
            case "lookup":
                return exec_lookup(node);
        }
        return null;
    }

    // return value for functions only
    // default 0 - other type of sequence type would use the return value (syntax rules)
    private Value exec_sequence(StatementParse node){
        for (int i = 0; i < node.getChildren().size(); i++){
            if (node.getChildren().get(i).getName().equals("return")){
                return exec(node.getChildren().get(i));
            }
            exec(node.getChildren().get(i));
        }
        return new IntegerValue(0);
    }

    // TODO handle return value
    private Value exec_return(StatementParse node){
        StatementParse ret = node.getChildren().get(0);
        // if it's a function, return the closure
        if (ret.getName().equals("function")){
            System.out.println("returned a function");
            return new Closure(currentClosure, ret);
        }
        // if it's a function call, exec the function, get its return value
        else if (ret.getName().equals("call")){
            // TODO assumed calling a variable
            System.out.println("returned a function call");
            return exec(ret);
            //Closure function = (Closure) exec_get_value(ret.getChildren().get(0));
            //currentClosure.setRet(function.getRet());
        }
        else {
            System.out.println("returned an integer " + eval(ret));
            return new IntegerValue(eval(ret));
        }
    }

    private void exec_statement(StatementParse node){
        exec(node.getChildren().get(0));
    }

    private String exec_expression(StatementParse node){
        return eval(node).toString();
    }

    private String exec_print(StatementParse node){
        StatementParse value = node.getChildren().get(0);
        String output = "";
        if (operators.contains(value.getName())){
            output = exec_expression(value) + "\n";
        } else if (value.getName().equals("lookup")){
            Value variable = exec_get_value(value);
            output = variable + "\n";
        } else {
            exec(value);
        }
        System.out.println("print: " + output);
        consoleOutput = consoleOutput.concat(output);
        return output;
    }

    // function - (call (lookup name) (arguments x,y))
    // get return value - immediately after exec_call, access Closure.ret
    private Value exec_call(StatementParse node){
        System.out.println("Attempt to call function");
        // TODO not lookup, function directly
        StatementParse lookup = node.getChildren().get(0);
        StatementParse arguments = node.getChildren().get(1);
        Value value = exec_get_value(lookup);
        if (value instanceof Closure){
            Closure closure = (Closure) value;
            // if number of parameter is incorrect
            if (closure.getParameters().getChildren().size() != arguments.getChildren().size()){
                throw new ArgumentMismatch();
            }
            String result = "";
            // switch the current closure to the function's closure
            // exec the function, then switch back to the current closure
            Closure old = currentClosure;
            currentClosure = closure;
            // declare the arguments inside the closure using parameter as variable name
            for (int i = 0; i < arguments.getChildren().size(); i++){
                String parameter = currentClosure.getParameters().getChildren().get(i).getName();
                System.out.println("declaring parameter inside closure: " + parameter);
                // TODO arguments could be not integers
                currentClosure.declare(parameter, eval(arguments.getChildren().get(i)));
            }
            Value ret = exec(currentClosure.getSequence());
            currentClosure.closeClosure();
            currentClosure = old;
            System.out.println("called a function");
            return ret;
        }
        throw new CallingNonFunction();
    }

    // (declare name value)
    // value is optional, either an integer or a function
    private void exec_declare(StatementParse node){
        String variableName = node.getChildren().get(0).getName();
        System.out.println("attempt declare: " + variableName);
        // if value is included
        if (node.getChildren().size() == 2){
            StatementParse value = node.getChildren().get(1);
            if (value.getName().equals("function")){
                currentClosure.declare(variableName, value, currentClosure);
                System.out.println("declared a function: " + variableName);
            } else if (value.getName().equals("call")){
                Value ret = exec(value);
                if (ret instanceof Closure){
                    currentClosure.declare(variableName, ((Closure) ret).getFunction(), ((Closure) ret).getParent());
                } else { // a IntegerValue
                    currentClosure.declare(variableName, ((IntegerValue) ret).getValue());
                }
                System.out.println("declared using a function call: " + variableName);
            }
            else {
                currentClosure.declare(variableName, eval(value));
                System.out.println("declared an integer variable: " + variableName);
            }
        } else {
            System.out.println("declared a variable without value: "  + variableName);
            currentClosure.declare(variableName);
        }
    }

    // return the Value (Closure or IntegerValue)
    private Value exec_lookup(StatementParse node){
        System.out.println("lookup Value: " + node.getChildren().get(0).getName() );
        return currentClosure.lookup(node.getChildren().get(0).getName());
    }

    private void exec_assign(StatementParse node){
        // get the variable name in (varloc name)
        String name = node.getChildren().get(0).getChildren().get(0).getName();
        StatementParse value = node.getChildren().get(1);
        System.out.println("attempt assign: " + name);
        if (value.getName().equals("function")){
            currentClosure.assign(name, value, currentClosure);
            System.out.println("assigned a function: name");
        } else if (value.getName().equals("call")){
            Value ret = exec(value);
            if (ret instanceof Closure){
                currentClosure.assign(name, ((Closure) ret).getFunction(), ((Closure) ret).getParent());
            } else { // a IntegerValue
                currentClosure.assign(name, ((IntegerValue) ret).getValue());
            }
            System.out.println("assigned using a function call: " + name);
        } else {
            currentClosure.assign(name, eval(value));
            System.out.println("assigned an integer");
        }
    }

    private void exec_if(StatementParse node){
        // if the condition is true
        // create a child closure, set it as the currentClosure
        if (isTrue(eval(node.getChildren().get(0)))){
            currentClosure = new Closure(currentClosure);
            exec(node.getChildren().get(1));

            // change the closure back
            currentClosure = currentClosure.getParent();
        }
    }

    private void exec_ifelse(StatementParse node){
        // if the condition is true
        // create a child closure, set it as the currentClosure
        if (isTrue(eval(node.getChildren().get(0)))){
            currentClosure = new Closure(currentClosure);
            exec(node.getChildren().get(1));

            // change the closure back
            currentClosure = currentClosure.getParent();
        } else {
            currentClosure = new Closure(currentClosure);
            exec(node.getChildren().get(2));

            // change the closure back
            currentClosure = currentClosure.getParent();
        }
    }

    private void exec_while(StatementParse node){
        // if the condition is true
        // create a child closure, set it as the currentClosure
        while (isTrue(eval(node.getChildren().get(0)))){
            currentClosure = new Closure(currentClosure);
            exec(node.getChildren().get(1));

            // change the closure back
            currentClosure = currentClosure.getParent();
        }
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

    // look up integer only
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
}

class UndefinedVariable extends RuntimeException{
    UndefinedVariable(){
        super();
    }
}

class MathOnFunctions extends RuntimeException{
    MathOnFunctions(){
        super();
    }
}

class ReturnOutsideFunction extends RuntimeException{
    ReturnOutsideFunction(){
        super();
    }
}

class DuplicateParam extends RuntimeException{
    DuplicateParam(){
        super();
    }
}

class CallingNonFunction extends RuntimeException{
    CallingNonFunction(){
        super();
    }
}

class ArgumentMismatch extends RuntimeException{
    ArgumentMismatch(){
        super();
    }
}