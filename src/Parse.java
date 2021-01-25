public class Parse {

    // the value of the parse
    private int value;
    // the number of characters into the string we've parsed
    private int index;

    public Parse(int value, int index) {
        this.value = value;
        this.index = index;
    }

    public boolean equals(Parse other) {
        return (this.value == other.value) && (this.index == other.index);
    }

    public String toString() {
        return "Parse(" + this.value + ", " + this.index + ")";
    }

    public int getValue() {
        return this.value;
    }

    public int getIndex() {
        return this.index;
    }

}