import java.util.HashMap;

// throw runtime exception
public class Closure {
    private HashMap<String, Integer> closure;
    private Closure parent;

    public Closure(){
        this.closure = new HashMap<>();
    }

    public Closure(Closure parent){
        this.closure = new HashMap<>();
        this.parent = parent;
    }

    public HashMap<String, Integer> getClosure() {
        return closure;
    }

    public boolean contains (String variable){
        return closure.containsKey(variable);
    }

    // declare a new variable
    // default value to 0
    public void declare (String name){
        if (contains(name)){
            throw new VariableAlreadyDefined();
        }
        this.closure.put(name, 0);
    }

    // declare with value
    public void declare (String name, int value){
        if (contains(name)){
            throw new VariableAlreadyDefined();
        }
        this.closure.put(name, value);
    }

    public void assign (String name, int value){
        if (!contains(name)){
            throw new UndefinedVariable();
        }
        this.closure.put(name, value);
    }

    public int lookup (String name){
        if (!contains(name)){
            throw new UndefinedVariable();
        }
        return this.closure.get(name);
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
}
