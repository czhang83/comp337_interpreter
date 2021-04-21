import java.util.HashMap;

// a class
// var can not be changed afterward
public class Environment extends Closure{

    private Closure parent;
    private StatementParse node;

    public Environment(){}

    // when className() is initialized, create a new EnvObject in exec
    public Environment(Closure parent, StatementParse node){
        this.parent = parent;
        this.node = node;
    }

    // override assign to give out error


    public String toString() { return "class"; }


}
