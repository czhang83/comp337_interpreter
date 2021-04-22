import java.util.HashMap;

// a class
// var can not be changed afterward
public class Environment extends Closure{ //TODO clean up inheritance

    private Closure parent;
    private StatementParse node;

    public Environment(){}

    // when className() is initialized, create a new EnvObject in exec
    public Environment(Closure parent, StatementParse node){
        this.parent = parent;
        this.node = node;
    }

    // TODO override assign to give out error

    public EnvironmentObject create_object(){
        return new EnvironmentObject(this);
    }

    @Override
    public StatementParse getParameters(){
        throw new RuntimeException("Accessing parameters of a class");
    }

    @Override
    public StatementParse getSequence(){
        throw new RuntimeException("Accessing sequence of a class");
    }


    public String toString() { return "class"; }


    @Override
    public Closure getParent() {
        return parent;
    }

    @Override
    public StatementParse getNode() {
        return node;
    }

    public void setNode(StatementParse node) {
        this.node = node;
    }
}
