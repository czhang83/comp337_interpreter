public class IntegerValue extends Value {

    private Integer value;
    public IntegerValue(int value){
        this.value = value;
    }


    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return getValue().toString();
    }
}
