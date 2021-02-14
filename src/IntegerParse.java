import java.util.List;

public class IntegerParse extends StatementParse { // no children

    private int value;
    public IntegerParse(int value, int index){
        super("integer", index);
        this.value = value;
    }

    public String toString(){
        return "" + this.value;
    }

    public int getValue() {
        return this.value;
    }

    public boolean integer_equals(IntegerParse other) {
        return (this.getValue() == other.getValue()) && (this.getIndex() == other.getIndex());
    }
}
