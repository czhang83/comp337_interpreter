import java.util.ArrayList;
import java.util.List;

public class StatementParse extends Parse{

    private List<StatementParse> children;

    // for Transform
    private boolean negative = false;

    public StatementParse(String name){
        super(name);
        children = new ArrayList<>();
    }

    public StatementParse(String name, int index){
        super(name, index);
        children = new ArrayList<>();
    }

    public List<StatementParse> getChildren(){
        return this.children;
    }

    public void setChildren(List<StatementParse> newChildren){
        this.children = newChildren;
    }

    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append("(");
        result.append(this.getName());
        for (StatementParse child : this.children){
            result.append(" ").append(child.toString());
        }
        result.append(")");
        return result.toString();
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    public void changeNegativity() {
        this.negative = !this.negative;
    }
}
