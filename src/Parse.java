//Parse have the value for the current scope, and index for the string
public class Parse {

    // statement parse - name, index, create new list of children

    private String name;
    private int index;

    public Parse(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public boolean equals(Parse other) {
        return (this.name.equals(other.name)) && (this.index == other.index);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    @Override
    public String toString() {
        return "";
    }
}