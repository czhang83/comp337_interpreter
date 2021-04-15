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
            consoleOutput = consoleOutput.concat("runtime error: divide by zero");
        } catch (VariableAlreadyDefined e){
            consoleOutput = consoleOutput.concat("runtime error: variable already defined");
        } catch (UndefinedVariable e){
            consoleOutput = consoleOutput.concat("runtime error: undefined variable");
        } catch (DuplicateParam e){
            consoleOutput = consoleOutput.concat("runtime error: duplicate parameter");
        }

        catch (CallingNonFunction e){
            consoleOutput = consoleOutput.concat("runtime error: calling a non-function");
        } catch (ArgumentMismatch e){
            consoleOutput = consoleOutput.concat("runtime error: argument mismatch");
        } catch (MathOnFunctions e){
            consoleOutput = consoleOutput.concat("runtime error: math operation on functions");
        } catch (ReturnOutsideFunction e){
            consoleOutput = consoleOutput.concat("runtime error: returning outside function");
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
        if (operators.contains(statementNode.getName())){
            evaluate(statementNode);
        }
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
                return exec_if(statementNode);
            case "ifelse":
                return exec_ifelse(statementNode);
            case "while":
                return exec_while(statementNode);
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
            case "call":
                return exec(node);
            case "function":
                return new Closure(currentClosure, node);
        }
        return null;
    }

    // return value for functions only
    // default 0 - other type of sequence type would use the return value (syntax rules)
    private Value exec_sequence(StatementParse node){
        Value ret = null;
        for (int i = 0; i < node.getChildren().size(); i++){
            if (node.getChildren().get(i).getName().equals("return")){
                ret = exec(node.getChildren().get(i));
                currentClosure.returningToTrue();
                break;
            }
            ret = exec(node.getChildren().get(i));
            if (currentClosure.isReturning()){
                break;
            }
        }

        if (ret == null && currentClosure.isInFunction()){
            /**System.out.println("----------------------------------------returing 0");
             for (int i = 0; i < node.getChildren().size(); i++){
             System.out.println(node.getChildren().get(i));
             }*/
            return new IntegerValue(0);
        }
        return ret;
    }

    private Value exec_return(StatementParse node){
        if (!currentClosure.isInFunction()){
            throw new ReturnOutsideFunction();
        }
        StatementParse ret = node.getChildren().get(0);
        // if it's a function, return the closure
        switch (ret.getName()) {
            case "function":
                System.out.println("returned a function");
                return new Closure(currentClosure, ret);
            // if it's a function call, exec the function, get its return value
            case "call":
                System.out.println("returned a function call");
                return exec(ret);
            case "lookup":
                System.out.println("returned a variable");
                return exec_get_value(ret);
            default:
                System.out.println("returned an integer " + evaluate(ret));
                return new IntegerValue(evaluate(ret));
        }
    }

    private void exec_statement(StatementParse node){
        exec(node.getChildren().get(0));
    }

    private String exec_expression(StatementParse node){
        return evaluate(node).toString();
    }

    private void exec_print(StatementParse node){
        StatementParse value = node.getChildren().get(0);
        String output;
        if (operators.contains(value.getName())){
            output = exec_expression(value) + "\n";
        } else if (value.getName().equals("lookup")){
            Value variable = exec_get_value(value);
            output = variable + "\n";
        } else {
            //output = exec(value) + "\n";
            Value result = exec(value);
            System.out.println("Inside print " + result.getClass());
            output = result + "\n";
        }
        System.out.println("print: " + output);
        consoleOutput = consoleOutput.concat(output);
    }

    // function - (call (lookup name) (arguments x,y))
    // get return value - immediately after exec_call, access Closure.ret
    // or (call (call (...)))
    private Value exec_call(StatementParse node){
        System.out.println("Attempt to call function");
        Value value;
        // TODO not lookup, function directly
        // lookup - could be lookup, call, or function
        StatementParse lookup = node.getChildren().get(0);
        StatementParse arguments = node.getChildren().get(1);
        // (call (call (...)))
        if (lookup.getName().equals("call")){
            value = exec(node.getChildren().get(0));
        } else if (lookup.getName().equals("function")) {
            // (call (function (...)))
            value = new Closure(currentClosure, lookup);
        } else{
            value = exec_get_value(lookup);
        }
        if (value instanceof Closure){
            Closure closure = ((Closure) value).copy();
            // if number of parameter is incorrect
            if (closure.getParameters().getChildren().size() != arguments.getChildren().size()){
                throw new ArgumentMismatch();
            }
            // switch the current closure to the function's closure
            // exec the function, then switch back to the current closure

            // declare the arguments inside the closure using parameter as variable name

            // pass in function parameter - copy or actual object
            // currently a copy of integer, use the same Closure object
            for (int i = 0; i < arguments.getChildren().size(); i++){
                String parameter = closure.getParameters().getChildren().get(i).getName();
                System.out.println("declaring parameter inside closure: " + parameter);
                Value paramValue = exec_get_value(arguments.getChildren().get(i));
                if (paramValue instanceof Closure){
                    closure.declare(parameter, (Closure) paramValue);
                } else if (paramValue instanceof IntegerValue){
                    closure.declare(parameter, ((IntegerValue) paramValue).getValue());
                } else { // if returns null - not an Integer or Closure
                    // treat it as an expression
                    closure.declare(parameter, evaluate(arguments.getChildren().get(i)));
                }
                // TODO input class object
            }
            // search arguments in the closure that called the function (currentClosure)
            // after declaring the parameters in the function closure
            // change the currentClosure to the function closure to exec
            Closure old = currentClosure;
            currentClosure = closure;
            System.out.println("entered a closure " + currentClosure.isInFunction());
            Value ret = exec(currentClosure.getSequence());
            currentClosure = old;
            System.out.println("exited a closure");
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
                currentClosure.declare(variableName, evaluate(value));
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
            Integer number = evaluate(value);
            currentClosure.assign(name, number);
            System.out.println("assigned an integer " + number);
        }
    }

    private Value exec_if(StatementParse node){
        System.out.println("enter an if");
        // a function could return inside control flow
        Value ret = null;
        // if the condition is true
        // create a child closure, set it as the currentClosure
        if (isTrue(evaluate(node.getChildren().get(0)))){
            System.out.println("if condition true");
            currentClosure = new Closure(currentClosure);
            ret = exec(node.getChildren().get(1));

            // change the closure back
            currentClosure = currentClosure.getParent();
        }
        return ret;
    }

    private Value exec_ifelse(StatementParse node){
        System.out.println("enter an ifelse");
        Value ret;
        // if the condition is true
        // create a child closure, set it as the currentClosure
        if (isTrue(evaluate(node.getChildren().get(0)))){
            System.out.println("if condition true");
            currentClosure = new Closure(currentClosure);
            ret = exec(node.getChildren().get(1));

            // change the closure back
            currentClosure = currentClosure.getParent();
        } else {
            System.out.println("else condition true");
            currentClosure = new Closure(currentClosure);
            ret = exec(node.getChildren().get(2));

            // change the closure back
            currentClosure = currentClosure.getParent();
        }
        return ret;
    }

    private Value exec_while(StatementParse node){
        System.out.println("enter an while");
        Value ret = null;
        // if the condition is true
        // create a child closure, set it as the currentClosure
        while (isTrue(evaluate(node.getChildren().get(0)))){
            // while should terminate if return from inside
            if (currentClosure.isReturning()) return ret;
            System.out.println("while condition true");
            currentClosure = new Closure(currentClosure);
            ret = exec(node.getChildren().get(1));

            // change the closure back
            currentClosure = currentClosure.getParent();
        }
        return ret;
    }

    // called outside of evals
    // when expression is only (lookup x), return 1 when x is a function
    public Integer evaluate(StatementParse node){
        if (node.getName().equals("lookup")){
            int value;
            try {
                value = eval(node);
            } catch (MathOnFunctions e){
                return 1;
            }
            return value;
        }
        return eval(node);
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
            case "call": // for function calls in expressions
                Value result = exec(node);
                if (result instanceof Closure) throw new MathOnFunctions();
                return ((IntegerValue) result).getValue();
            default:
                return 0;
        }
    }

    private Integer eval_eq(StatementParse node){
        Integer first;
        Integer second;
        try{
            first = eval(node.getChildren().get(0));
            second = eval(node.getChildren().get(1));
        } catch (MathOnFunctions e){
            Value func1 = exec_get_value(node.getChildren().get(0));
            Value func2 = exec_get_value(node.getChildren().get(1));
            if (func1 == func2) return 1;
            return 0;
        }
        if (first.equals(second)) return 1;
        return 0;
    }
    private Integer eval_not_eq(StatementParse node){
        if (eval_eq(node) == 1) return 0;
        return 1;
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
        if (isTrue(node.getChildren().get(0))) return 0;
        return 1;
    }
    private Integer eval_or(StatementParse node){
        if (isTrue(node.getChildren().get(0)) || isTrue(node.getChildren().get(1))) return 1;
        return 0;
    }
    private Integer eval_and(StatementParse node){
        if (isTrue(node.getChildren().get(0)) && isTrue(node.getChildren().get(1))) return 1;
        return 0;
    }

    // look up integer only
    private Integer eval_lookup(StatementParse node){
        System.out.println("look up in eval: " + node.getChildren().get(0).getName());
         Value value = currentClosure.lookup(node.getChildren().get(0).getName());
         if (value instanceof IntegerValue){
             return ((IntegerValue) value).getValue();
         } else if (value instanceof Closure){ // isTrue will catch this error for the boolean operations
             throw new MathOnFunctions();
         }
         // TODO operations on classes
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

    // isTrue only used in boolean functions (eval_or, eval_and, etc)
    // eval_lookup will always throw MathOnFunctions when given a function
    // isTrue catch it and return true for the boolean functions
    // evaluation will handle the case when the expression only contains (lookup function), not using boolean functions
    private Boolean isTrue(StatementParse node){
        System.out.println("isTrue function triggered");
        int value;
        try {
            value = eval(node);
        } catch (MathOnFunctions e){
            return true;
        }
        return isTrue(value);
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