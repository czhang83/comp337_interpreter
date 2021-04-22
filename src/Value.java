public abstract class Value {

    // give error
    // EnvObj override this
    public Value lookup_member(String name){
        throw new NonObjectMember();
    }

}
