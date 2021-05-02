import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ConstantFoldingTransform {

    HashSet<String> operators = new HashSet<>(){{addAll(
            Arrays.asList("+", "-", "*", "/"));
    }};

    public boolean isAddSub(StatementParse node) {
        return node.getName().equals("+") || node.getName().equals("-");
    }

    boolean isMulDiv(StatementParse node) {
        return node.getName().equals("*") || node.getName().equals("/");
    }


    public StatementParse visit(Parse node) {
        StatementParse statementNode = (StatementParse) node;
        if (node instanceof IntegerParse) {
            return statementNode;
        }

        List<StatementParse> newChildren = new ArrayList<>();
        for (StatementParse child : statementNode.getChildren()) {
            child = this.visit(child);
            if (!this.isAddSub(statementNode) && this.isAddSub(child)) {
                child = this.addSubTransform(child);
            }
            newChildren.add(child);
        }
        statementNode.setChildren(newChildren);
        if (this.isMulDiv(statementNode)) {
            return this.mulDivTransform(statementNode);
        } else {
            return statementNode;
        }
    }

    public StatementParse addSubTransform(StatementParse node) {
        // This function is called on the _top_ addition or subtraction node.
        // "Top" here means that if the node's parent is _not_ an addition or
        // subtraction node. So for (print (+ 2 (* 3 (+ 4 5)))), this function
        // will be called twice - once for (+ 4 5), and once for (+ 2 ... ).

        ArrayList<StatementParse> expanded = this.expandNode(node);
        expanded = this.nonConstantFirst(expanded);
        expanded = this.collapseExpression(expanded);
        expanded = this.positiveFirst(expanded);
        node = this.reconstructTree(expanded);
        return node;
    }

    // don't rearrange
    // only combine node when both node are integers
    // leave divided by zero alone
    public StatementParse mulDivTransform(StatementParse node) {
        // This function is called on _every_ multiply and divide node.
        StatementParse node1 = node.getChildren().get(0);
        StatementParse node2 = node.getChildren().get(1);
        if (!(node1 instanceof IntegerParse) || !(node2 instanceof IntegerParse)){
            return node;
        }
        int value1 = ((IntegerParse) node1).getValue();
        int value2 = ((IntegerParse) node2).getValue();
        int result;
        if (node.getName().equals("*")){
            result = value1 * value2;
        } else{ // div node
            if (value2 == 0) return node;
            result = value1 / value2;
        }

        IntegerParse newNode = new IntegerParse(result);
        if (node.isNegative()) newNode.setNegative(true);
        return newNode;
    }

    // expand a node tree into a flat list
    // each element is a either an integer, or a non-constant that is not +- (a variable, */)
    // transform */ nodes first, check if it can becomes a integer node.
    // the second node of a sub node becomes negative
    public ArrayList<StatementParse> expandNode(StatementParse node){
        ArrayList<StatementParse> expanded = new ArrayList<>();
        recursiveExpand(node, expanded);
        return expanded;

    }

    public void recursiveExpand(StatementParse node, ArrayList<StatementParse> expanded){
        // make the second node negative in a - node.
        if (node.getName().equals("-")){
            node.getChildren().get(1).changeNegativity();
        }
        for (StatementParse child: node.getChildren()){
            // distribute if the child is negative
            if (child.isNegative()){
                for (StatementParse grandChild: child.getChildren()){
                    grandChild.changeNegativity();
                }
            }
            if (this.isMulDiv(child)) {
                StatementParse transformedMulDiv = this.mulDivTransform(child);
                expanded.add(transformedMulDiv);
            } else if (this.isAddSub(child)){
                recursiveExpand(child, expanded);
            } else {
                expanded.add(child);
            }
        }
    }

    // move all non-constant node to the front
    // keep the original order
    public ArrayList<StatementParse> nonConstantFirst (ArrayList<StatementParse> expression){
        ArrayList<StatementParse> reordered = new ArrayList<>();
        for (int i = expression.size()-1; i >= 0; i--){
            if (!(expression.get(i) instanceof IntegerParse)){
                reordered.add(0, expression.get(i));
                expression.remove(i);
            }
        }
        // add the integers to the end
        reordered.addAll(expression);
        return reordered;

    }

    // given a expression with all non-constant in the front
    // collapse the constant values
    public ArrayList<StatementParse> collapseExpression(ArrayList<StatementParse> expression){
        int index = 0;
        for (int i = 0; i < expression.size(); i++){
            if (expression.get(i) instanceof IntegerParse) {
                index = i;
                break;
            }
            // when all nodes are not constant
            if (i == expression.size() - 1){
                index = expression.size();
            }
        }

        int result = 0;
        for (int i = index; i < expression.size(); i++){
            StatementParse child = expression.get(i);
            if (child.isNegative()){
                result = result - ((IntegerParse) child).getValue();
            } else {
                result = result + ((IntegerParse) child).getValue();
            }
        }
        ArrayList<StatementParse> converted = new ArrayList<>(expression.subList(0, index));
        // collapse 0
        if (result == 0) return converted;
        // if result is negative, flip the sign
        IntegerParse integer;
        if (result < 0){
            result = - result;
            integer = new IntegerParse(result);
            integer.setNegative(true);
        } else {
            integer = new IntegerParse(result);
        }
        converted.add(integer);
        return converted;
    }

    // move the first positive node to the front
    public ArrayList<StatementParse> positiveFirst(ArrayList<StatementParse> expression){
        for (int i = 0; i < expression.size(); i++){
            if (expression.get(i).isNegative()) continue;

            // move the first positive to the front
            StatementParse positive = expression.get(i);
            expression.remove(i);
            expression.add(0, positive);
            return expression;
        }
        // if no positive node exist, or the collapsed array is empty
        // insert a 0 in the front
        expression.add(0, new IntegerParse(0));
        return expression;
    }

    // convert the array into a tree
    // collapse zero that is not in the front
    public StatementParse reconstructTree(ArrayList<StatementParse> expression){
        // an empty expression will not trigger this method
        // first element is always an integer
        StatementParse left_node = expression.get(0);

        for (int i = 1; i < expression.size(); i++){
            StatementParse right_node = expression.get(i);
            if (right_node instanceof IntegerParse && ((IntegerParse) right_node).getValue() == 0){
                continue;
            }
            StatementParse top;
            if (right_node.isNegative()){
                top = new StatementParse("-");
            } else {
                top = new StatementParse("+");
            }
            top.getChildren().add(left_node);
            top.getChildren().add(right_node);
            left_node = top;
        }
        return left_node;
    }

    public void print_array(ArrayList<StatementParse> list){
        for (StatementParse child : list){
            if (child.isNegative()){
                System.out.print("[ - " + child + "]");
            } else {
                System.out.print("[" + child + "]");
            }
        }
        System.out.println();
    }

}