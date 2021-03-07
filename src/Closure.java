import java.util.HashMap;

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

    public void setClosure(HashMap<String, Integer> closure) {
        this.closure = closure;
    }

    public Closure getParent() {
        return parent;
    }

    public void setParent(Closure parent) {
        this.parent = parent;
    }
}
