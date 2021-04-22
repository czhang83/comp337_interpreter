import java.util.HashMap;

// an object of a certain class
// cannot add var
// functions - closure where the class is defined
// add 'this' variable to the member function closure

// parent never accessed except during initialization - set to class's parent
// member function parent set to this obj - to access other class member
// Closure order: member function -> obj -> class's parent
// when initializing, change currentClosure to EnvObj to declare the variables
public class EnvironmentObject extends Environment{

    private HashMap<String, Value> values; // no separate closure
    private Environment belongedClass;
    private Closure parent;

    public EnvironmentObject(Environment belongedClass) {
        this.values = new HashMap<>();
        this.belongedClass = belongedClass;
        this.parent = belongedClass.getParent();
    }

    // declare with value
    @Override
    public void declare (String name, int value){
        if (contains(name)){
            throw new VariableAlreadyDefined();
        }
        this.values.put(name, new IntegerValue(value));
    }

    // declare with function - during initialization only, otherwise error
    // functions - closure is where the class is defined
    // TODO can not declare after initialization - prohibited by parser?
    @Override
    public void declare (String name, StatementParse node, Closure parent){
        parent = this;
        if (contains(name)){
            throw new VariableAlreadyDefined();
        }
        Closure newClosure;
        if (node.getName().equals("function")){
            newClosure = new Closure(parent, node);
            newClosure.setBelongObject(this);
        } else {
            newClosure = new Environment(parent, node);
        }

        this.values.put(name, newClosure);
    }

    // declare a function using a Closure object
    // used for declaring parameters inside functions
    @Override
    public void declare (String name, Value value){
        if (contains(name)){
            throw new VariableAlreadyDefined();
        }
        this.values.put(name, value);
    }

    // TODO don't need assign? only triggered when inside this closure, which don't happen unless accessing member variables
    // assign a new int
    @Override
    public void assign (String name, int value){
        Closure exist = find_var(name);
        exist.getValues().put(name, new IntegerValue(value));
    }

    @Override
    // assign a new function / class
    public void assign (String name, StatementParse node, Closure parent){
        parent = this;
        Closure exist = find_var(name);
        Closure newClosure;
        if (node.getName().equals("function")){
            newClosure = new Closure(parent, node);
            newClosure.setBelongObject(this);
        } else {
            newClosure = new Environment(parent, node);
        }
        exist.getValues().put(name, newClosure);
    }

    @Override
    // assign using a Closure object
    // used for declaring parameters inside functions
    public void assign (String name, Value value){
        Closure exist = find_var(name);
        exist.getValues().put(name, value);
    }

    @Override
    public Value lookup_member(String name){
        if (!this.contains(name)){
            throw new UndefinedMember();
        }
        return this.getValues().get(name);
    }

    @Override
    // if variable not exist, check the parents, until reached the end
    public Value lookup (String name){
        Closure exist = find_var(name);
        return exist.getValues().get(name);
    }

    @Override
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

    @Override
    public String toString() { return "obj"; }

    @Override
    public HashMap<String, Value> getValues() {
        return values;
    }

    public Environment getBelongedClass() {
        return belongedClass;
    }

    @Override
    public boolean contains (String variable){
        return values.containsKey(variable);
    }

    @Override
    public Closure getParent() {
        return parent;
    }

    @Override
    public void setParent(Closure parent) {
        this.parent = parent;
    }
}

