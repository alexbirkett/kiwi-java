package no.birkett.kiwi;

/**
 * Created by alex on 30/01/15.
 */
public class Term {

    private Variable variable;
    double coefficient;

    public Term(Variable variable, double coefficient) {
        this.variable = variable;
        this.coefficient = coefficient;
    }

    public Term(Variable variable) {
        this(variable, 1.0);
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    public double getValue() {
        return coefficient * variable.getValue();
    }

    @Override
    public String toString() {
        return "variable: (" + variable + ") coefficient: "  + coefficient;
    }
}
