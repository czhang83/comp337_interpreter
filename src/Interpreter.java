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

    // syntax error - Parser return null
    public String execute (Parse node){
        mainClosure = new Closure();
        currentClosure = mainClosure;

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

        catch (NonObjectMember e){
            consoleOutput = consoleOutput.concat("runtime error: member of non-object");
        } catch (UndefinedMember e){
            consoleOutput = consoleOutput.concat("runtime error: undefined member");
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
            case "member":
                return exec_member(statementNode);
            case "return":
                return exec_return(statementNode);
        }
        return null;
    }

    public Value exec_get_value(StatementParse node) {
        switch (node.getName()) {
            case "lookup":
                return exec_lookup(node);
            case "member":
                return exec_member(node);
            case "call":
                return exec(node);
            case "function":
                return new Closure(currentClosure, node);
            default: // an int:
                return new IntegerValue(evaluate(node));
        }
    }

    // given (memloc (varloc objName) name)) or (memloc (memloc (...)))
    // get the member variable
    // used in assign to provide the inner memloc obj
    public Value exec_get_loc(StatementParse node){
        StatementParse varloc = node.getChildren().get(0);
        String name = node.getChildren().get(1).getName();
        if (varloc.getName().equals("varloc")){
            String objName = varloc.getChildren().get(0).getName();
            Closure target_obj = (Closure) currentClosure.lookup(objName);
            if (!(target_obj instanceof EnvironmentObject)) throw new NonObjectMember();
            return target_obj.lookup_member(name);
        }

        Closure target_obj = (Closure) exec_get_loc(varloc);
        return target_obj.lookup_member(name);
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
                return new Closure(currentClosure, ret);
            case "class":
                return new Environment(currentClosure, ret);
            // if it's a function call, exec the function, get its return value
            case "call":
                return exec(ret);
            case "lookup":
                return exec_get_value(ret);
            default:
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
        } else if (value.getName().equals("lookup") || value.getName().equals("member")){
            Value variable = exec_get_value(value);
            output = variable + "\n";
        } else {
            //output = exec(value) + "\n";
            Value result = exec(value);
            output = result + "\n";
        }
        consoleOutput = consoleOutput.concat(output);
    }

    // function - (call (lookup name) (arguments x,y))
    // class - (call (lookup className) (arguments))) - return an class object
    // or (call (call (...)))
    private Value exec_call(StatementParse node){
        Value value;
        // lookup - could be lookup, call, or function
        StatementParse lookup = node.getChildren().get(0);
        StatementParse arguments = node.getChildren().get(1);
        // (call (call (...)))
        switch (lookup.getName()) {
            case "call":
                value = exec(node.getChildren().get(0));
                break;
            case "function":  //TODO Simplify to exec_get_value?
                // (call (function (...)))
                value = new Closure(currentClosure, lookup);
                break;
            case "class":
                // (call (class (...)))
                value = new Environment(currentClosure, lookup);
                break;
            case "member":
                // (call (member (...)))
                value = exec_get_value(lookup);
                break;
            default:
                value = exec_get_value(lookup);
                break;
        }

        // can not call an object
        if (value instanceof EnvironmentObject){
            throw new CallingNonFunction();
        }else if(value instanceof Environment){ // if calling a class
            if (arguments.getChildren().size() != 0) throw new ArgumentMismatch();
            EnvironmentObject obj = ((Environment) value).create_object();
            Closure old = currentClosure;
            currentClosure = obj;
            // class children - declare statements
            // use exec_sequence to exec all declares
            exec_sequence(((Environment) value).getNode());
            currentClosure = old;
            return obj;
        }
        // a function - (call (lookup name) (arguments x,y))
        else if (value instanceof Closure){
            Closure closure = ((Closure) value).copy_function();
            int argumentIndex = 0;
            if (closure.isMember()){
                // if it's a member function, add 'this' to the argument
                // parameter should be (this,...)
                if (closure.getParameters().getChildren().size() < 1) throw new ArgumentMismatch();
                argumentIndex = 1;
                closure.declare(closure.getParameters().getChildren().get(0).getName(), closure.getBelongObject());
            }
            // if number of parameter is incorrect
            if (closure.getParameters().getChildren().size() != arguments.getChildren().size() + argumentIndex){
                throw new ArgumentMismatch();
            }
            // switch the current closure to the function's closure
            // exec the function, then switch back to the current closure

            // declare the arguments inside the closure using parameter as variable name

            // pass in function parameter - copy or actual object
            // currently a copy of integer, use the same Closure object
            for (int i = 0; i < arguments.getChildren().size(); i++){
                String parameter = closure.getParameters().getChildren().get(i + argumentIndex).getName();
                Value paramValue = exec_get_value(arguments.getChildren().get(i));
                if (paramValue instanceof Closure){
                    closure.declare(parameter, paramValue);
                } else if (paramValue instanceof IntegerValue){
                    closure.declare(parameter, ((IntegerValue) paramValue).getValue());
                } else { // if returns null - not an Integer or Closure
                    // treat it as an expression
                    closure.declare(parameter, evaluate(arguments.getChildren().get(i)));
                }
            }
            // search arguments in the closure that called the function (currentClosure)
            // after declaring the parameters in the function closure
            // change the currentClosure to the function closure to exec
            Closure old = currentClosure;
            currentClosure = closure;
            Value ret = exec(currentClosure.getSequence());
            currentClosure = old;
            return ret;
        }
        throw new CallingNonFunction();
    }

    // (declare name value)
    // a declare statement must have value
    // can not declare new member variable
    private void exec_declare(StatementParse node){
        String variableName = node.getChildren().get(0).getName();
        // if value is included
        if (node.getChildren().size() == 2){
            StatementParse value = node.getChildren().get(1);
            switch (value.getName()) {
                case "function":
                case "class":
                    currentClosure.declare(variableName, value, currentClosure);
                    break;
                case "call":
                case "member": {
                    Value ret = exec(value);
                    if (ret instanceof EnvironmentObject) { // return a obj
                        currentClosure.declare(variableName, ret);
                    } else if (ret instanceof Closure) { // return a function or class
                        currentClosure.declare(variableName, ret);
                    } else { // a IntegerValue
                        currentClosure.declare(variableName, ((IntegerValue) ret).getValue());
                    }
                    break;
                }
                case "lookup": {
                    Value ret = exec_get_value(value);
                    if (!(ret instanceof IntegerValue)) {
                        currentClosure.declare(variableName, ret);
                    } else {
                        currentClosure.declare(variableName, ((IntegerValue) ret).getValue());
                    }
                    break;
                }
                default:
                    currentClosure.declare(variableName, evaluate(value));
                    break;
            }
        }
    }

    // return the Value
    private Value exec_lookup(StatementParse node){
        return currentClosure.lookup(node.getChildren().get(0).getName());
    }

    private Value exec_member(StatementParse node){
        Value obj = exec_get_value(node.getChildren().get(0));
        if (!(obj instanceof EnvironmentObject)) throw new NonObjectMember();

        String member_name = node.getChildren().get(1).getName();
        return obj.lookup_member(member_name);
    }

    // (assign (memloc (varloc name) value)))
    // (assign (varloc name) value)
    private void exec_assign(StatementParse node){
        Closure old = currentClosure;
        Closure target_obj = currentClosure; // use if assigning memloc
        StatementParse locationNode = node.getChildren().get(0);
        String name;
        if (locationNode.getName().equals("memloc")){ // could be nested memloc
            if (locationNode.getChildren().get(0).getName().equals("memloc")){
                target_obj = (Closure) exec_get_loc(locationNode.getChildren().get(0));
            } else {
                String objName = locationNode.getChildren().get(0).getChildren().get(0).getName();
                target_obj = (Closure) currentClosure.lookup(objName);
            }
            name = locationNode.getChildren().get(1).getName();
            if (!(target_obj instanceof EnvironmentObject)) throw new NonObjectMember();
            currentClosure = target_obj;
            // check that it won't trigger 'undefined member'
            currentClosure.lookup_member(name);
        } else {
            // get the variable name in (varloc name)
            name = node.getChildren().get(0).getChildren().get(0).getName();
        }
        StatementParse value = node.getChildren().get(1);
        if (value.getName().equals("function")|| value.getName().equals("class")){
            currentClosure.assign(name, value, currentClosure);
        } else if (value.getName().equals("call")|| value.getName().equals("member")){
            // if value require exec in the current closure
            // change closure back
            currentClosure = old;
            Value ret = exec(value);
            currentClosure = target_obj;
            if (ret instanceof EnvironmentObject){ // return a obj
                currentClosure.assign(name, ret);
            } else if (ret instanceof Closure){ // return a function or class
                currentClosure.assign(name, ret);
            } else { // a IntegerValue
                currentClosure.assign(name, ((IntegerValue) ret).getValue());
            }
        } else { // lookup
            currentClosure = old;
            Value ret = exec_get_value(value);
            currentClosure = target_obj;
            if (!(ret instanceof IntegerValue)){
                currentClosure.assign(name, ret);
            } else {
                currentClosure.assign(name, ((IntegerValue) ret).getValue());
            }
        }
        currentClosure = old;
    }

    private Value exec_if(StatementParse node){
        // a function could return inside control flow
        Value ret = null;
        // if the condition is true
        // create a child closure, set it as the currentClosure
        if (isTrue(evaluate(node.getChildren().get(0)))){
            currentClosure = new Closure(currentClosure);
            ret = exec(node.getChildren().get(1));

            // change the closure back
            currentClosure = currentClosure.getParent();
        }
        return ret;
    }

    private Value exec_ifelse(StatementParse node){
        Value ret;
        // if the condition is true
        // create a child closure, set it as the currentClosure
        if (isTrue(evaluate(node.getChildren().get(0)))){
            currentClosure = new Closure(currentClosure);
            ret = exec(node.getChildren().get(1));

            // change the closure back
            currentClosure = currentClosure.getParent();
        } else {
            currentClosure = new Closure(currentClosure);
            ret = exec(node.getChildren().get(2));

            // change the closure back
            currentClosure = currentClosure.getParent();
        }
        return ret;
    }

    private Value exec_while(StatementParse node){
        Value ret = null;
        // if the condition is true
        // create a child closure, set it as the currentClosure
        while (isTrue(evaluate(node.getChildren().get(0)))){
            // while should terminate if return from inside
            if (currentClosure.isReturning()) return ret;
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
        if (node.getName().equals("lookup") || node.getName().equals("call")
                || node.getName().equals("member") || node.getName().equals("function")
                || node.getName().equals("class")){
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
            case "member":
                return eval_lookup(node);
            case "function":
            case "class":
                throw new MathOnFunctions();
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
         Value value = exec_get_value(node);
         if (value instanceof IntegerValue){
             return ((IntegerValue) value).getValue();
         } else if (value instanceof Closure){ // isTrue will catch this error for the boolean operations
             throw new MathOnFunctions();
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

    // isTrue only used in boolean functions (eval_or, eval_and, etc)
    // eval_lookup will always throw MathOnFunctions when given a function
    // isTrue catch it and return true for the boolean functions
    // evaluation will handle the case when the expression only contains (lookup function), not using boolean functions
    private Boolean isTrue(StatementParse node){
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


class NonObjectMember extends RuntimeException{
    NonObjectMember(){
        super();
    }
}

class UndefinedMember extends RuntimeException{
    UndefinedMember(){
        super();
    }
}