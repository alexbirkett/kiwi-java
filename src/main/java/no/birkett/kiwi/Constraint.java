package no.birkett.kiwi;

/**
 * Created by alex on 30/01/15.
 */
public class Constraint {

    private Expression expression;
    private double strength;
    private RelationalOperator op;

    public Constraint(){
    }

    public Constraint(Expression expr, RelationalOperator op) {
        this(expr, op, Strength.REQUIRED);
    }

    public Constraint(Expression expr, RelationalOperator op, double strength) {
        this.expression = expr;
        this.op = op;
        this.strength = Strength.clip(strength);
    }

    public Constraint(Constraint other, double strength) {
        this(other.expression, other.op, strength);
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public double getStrength() {
        return strength;
    }

    public Constraint setStrength(double strength) {
        this.strength = strength;
        return this;
    }

    public RelationalOperator getOp() {
        return op;
    }

    public void setOp(RelationalOperator op) {
        this.op = op;
    }

}
