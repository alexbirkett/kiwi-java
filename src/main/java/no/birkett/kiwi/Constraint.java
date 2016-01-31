package no.birkett.kiwi;

import java.util.*;

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
        this.expression = reduce(expr);
        this.op = op;
        this.strength = Strength.clip(strength);
    }

    public Constraint(Constraint other, double strength) {
        this(other.expression, other.op, strength);
    }

    private static Expression reduce(Expression expr){

        Map<Variable, Double> vars = new LinkedHashMap<>();
        for(Term term: expr.getTerms()){
            Double value = vars.get(term.getVariable());
            if(value == null){
                value = 0.0;
            }
            value += term.coefficient;
            vars.put(term.getVariable(), value);
        }

        List<Term> reducedTerms = new ArrayList<>();
        for(Variable variable: vars.keySet()){
            reducedTerms.add(new Term(variable, vars.get(variable)));
        }

        return new Expression(reducedTerms, expr.getConstant());
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

    @Override
    public String toString() {
        return "expression: (" + expression + ") strength: " + strength + " operator: " + op;
    }

}
