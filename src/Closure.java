import java.util.HashMap;
import java.util.List;

// throw runtime exception
public class Closure extends Value{
    private HashMap<String, Value> values;
    private Closure parent;
    // if this closure is for a function, store the function node
    private StatementParse function;
    // function return default 0
    private Value ret = new IntegerValue(0);

    public Closure(){
        this.values = new HashMap<>();
    }

    public Closure(Closure parent){
        this.values = new HashMap<>();
        this.parent = parent;
    }

    public Closure(Closure parent, StatementParse function){
        this.values = new HashMap<>();
        this.parent = parent;
        this.function = function;
    }

    public HashMap<String, Value> getValues() {
        return values;
    }

    public boolean contains (String variable){
        return values.containsKey(variable);
    }

    // declare a new variable
    public void declare (String name){
        if (contains(name)){
            throw new VariableAlreadyDefined();
        }
        this.values.put(name, null);
    }

    // declare with value
    public void declare (String name, int value){
        if (contains(name)){
            throw new VariableAlreadyDefined();
        }
        this.values.put(name, new IntegerValue(value));
    }

    // declare with function
    public void declare (String name, StatementParse function, Closure parent){
        if (contains(name)){
            throw new VariableAlreadyDefined();
        }
        List<StatementParse> parameters = function.getChildren().get(0).getChildren();
        for (StatementParse x: parameters){
            int count = 0;
            for (StatementParse y: parameters){
                if (x.getName().equals(y.getName())){
                    count++;
                }
            }
            if (count > 1) throw new DuplicateParam();
        }
        Closure newClosure = new Closure(parent, function);
        this.values.put(name, newClosure);
    }

    // assign a new int
    public void assign (String name, int value){
        Closure exist = find_var(name);
        Closure newClosure = new Closure(parent, function);
        exist.getValues().put(name, new IntegerValue(value));
    }

    // assign a new function
    public void assign (String name, StatementParse function, Closure parent){
        Closure exist = find_var(name);
        Closure newClosure = new Closure(parent, function);
        exist.getValues().put(name, newClosure);
    }

    // if variable not exist, check the parents, until reached the end
    public Value lookup (String name){
        Closure exist = find_var(name);
        return exist.getValues().get(name);
    }

    // find the closure that contain the variable
    public Closure find_var(String name){
        Closure currentClosure = this;
        while (currentClosure != null){
            if (currentClosure.contains(name)){
                return currentClosure;
            }
            currentClosure = currentClosure.getParent();
        }
        throw new UndefinedVariable();
    }

    public Closure getParent() {
        return parent;
    }

    public void setParent(Closure parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "closure";
    }

    public StatementParse getFunction(){
        return this.function;
    }

    public StatementParse getParameters(){
        return this.function.getChildren().get(0);
    }

    public StatementParse getSequence(){
        return this.function.getChildren().get(1);
    }

    public void setRet(Value value){
        this.ret = value;
    }

    public Value getRet(){
        return this.ret;
    }

    // remove all variables when this closure is closed
    public void closeClosure(){
        this.values = new HashMap<>();
    }
}
