import java.util.ArrayList;
import java.util.List;

public class StatementParse extends Parse{

    private List<StatementParse> children;
    public StatementParse(String name, int index){
        super(name, index);
        children = new ArrayList<>();
    }

    public List<StatementParse> getChildren(){
        return this.children;
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
}
