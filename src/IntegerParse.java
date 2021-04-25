
public class IntegerParse extends StatementParse { // no children

    // Values class -> Integer class, Closure class
    private int value;
    private boolean negative = false;
    public IntegerParse(int value, int index){
        super("integer", index);
        this.value = value;
    }

    public IntegerParse(int value){
        super("integer");
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

    @Override
    public boolean isNegative() {
        return negative;
    }

    @Override
    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    @Override
    public void changeNegativity() {
        this.negative = !this.negative;
    }
}
