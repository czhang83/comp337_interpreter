
public class IdentifierParse extends StatementParse { // no children

    public IdentifierParse(String name, int index){
        super(name, index);
    }

    // print var name directly, no parenthesis surrounding
    public String toString(){
        return "" + this.getName();
    }

}