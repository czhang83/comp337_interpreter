import java.util.HashMap;

// an object of a certain class
// cannot add var
// functions - closure where the class is defined
public class EnvironmentObject extends Environment{

    private Closure parent;
    private HashMap<String, Value> values; // no separate closure
    private Environment belongedClass;

    public EnvironmentObject(Closure parent, Environment belongedClass) {
        this.parent = parent;
        this.values = new HashMap<>();
        this.belongedClass = belongedClass;
    }

    // declare with function - during initialization only, otherwise error
    // functions - closure is where the class is defined
    @Override
    public void declare (String name, StatementParse node, Closure parent){
        parent = this.belongedClass.getParent();
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

    @Override
    // assign a new function / class
    public void assign (String name, StatementParse node, Closure parent){
        parent = this.belongedClass.getParent();
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

    public String toString() { return "obj"; }
}
