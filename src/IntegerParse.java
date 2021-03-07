
public class IntegerParse extends StatementParse { // no children

    // Values class -> Integer class, Closure class
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

    public boolean equals(IntegerParse other) {
        return (this.getValue() == other.getValue()) && (this.getIndex() == other.getIndex());
    }
}
