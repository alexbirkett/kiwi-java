package no.birkett.kiwi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 31/01/15.
 */
public class Symbolics {

    private Symbolics() {
    }

    // Variable multiply, divide, and unary invert
    public static Term multiply(Variable variable, double coefficient) {
        return new Term(variable, coefficient);
    }

    public static Term divide(Variable variable, double denominator) {
        return multiply(variable, (1.0 / denominator));
    }

    public static Term negate(Variable variable) {
        return multiply(variable, -1.0);
    }

    // Term multiply, divide, and unary invert
    public static Term multiply(Term term, double coefficient) {
        return new Term(term.getVariable(), term.getCoefficient() * coefficient);
    }

    public static Term divide(Term term, double denominator) {
        return multiply(term, (1.0 / denominator));
    }

    public static Term negate(Term term) {
        return multiply(term, -1.0);
    }

    // Expression multiply, divide, and unary invert
    public static Expression multiply(Expression expression, double coefficient) {

        List<Term> terms = new ArrayList<Term>();

        for (Term term : expression.getTerms()) {
            terms.add(multiply(term, coefficient));
        }

        // TODO Do we need to make a copy of the term objects in the array?
        return new Expression(terms, expression.getConstant() * coefficient);
    }

    public static Expression multiply(Expression expression1, Expression expression2) throws NonlinearExpressionException {
        if (expression1.isConstant()) {
            return multiply(expression1.getConstant(), expression2);
        } else if (expression2.isConstant()) {
            return multiply(expression2.getConstant(), expression1);
        } else {
            throw new NonlinearExpressionException();
        }
    }
    
    public static Expression divide(Expression expression, double denominator) {
        return multiply(expression, (1.0 / denominator));
    }

    public static Expression divide(Expression expression1, Expression expression2) throws NonlinearExpressionException {
        if (expression2.isConstant()) {
            return divide(expression1, expression2.getConstant());
        } else {
            throw new NonlinearExpressionException();
        }
    }

    public static Expression negate(Expression expression) {
        return multiply(expression, -1.0);
    }

    // Double multiply
    public static Expression multiply(double coefficient, Expression expression) {
        return multiply(expression, coefficient);
    }


    public static Term multiply(double coefficient, Term term) {
        return multiply(term, coefficient);
    }


    public static Term multiply(double coefficient, Variable variable) {
        return multiply(variable, coefficient);
    }

    // Expression add and subtract
    public static Expression add(Expression first, Expression second) {
        //TODO do we need to copy term objects?
        List<Term> terms = new ArrayList<Term>(first.getTerms().size() + second.getTerms().size());

        terms.addAll(first.getTerms());
        terms.addAll(second.getTerms());

        return new Expression(terms, first.getConstant() + second.getConstant());
    }

    public static Expression add(Expression first, Term second) {
        //TODO do we need to copy term objects?
        List<Term> terms = new ArrayList<Term>(first.getTerms().size() + 1);

        terms.addAll(first.getTerms());
        terms.add(second);

        return new Expression(terms, first.getConstant());
    }

    public static Expression add(Expression expression, Variable variable) {
        return add(expression, new Term(variable));
    }

    public static Expression add(Expression expression, double constant) {
        return new Expression(expression.getTerms(), expression.getConstant() + constant);
    }

    public static Expression subtract(Expression first, Expression second) {
        return add(first, negate(second));
    }

    public static Expression subtract(Expression expression, Term term) {
        return add(expression, negate(term));
    }

    public static Expression subtract(Expression expression, Variable variable) {
        return add(expression, negate(variable));
    }

    public static Expression subtract(Expression expression, double constant) {
        return add(expression, -constant);
    }

    // Term add and subtract
    public static Expression add(Term term, Expression expression) {
        return add(expression, term);
    }

    public static Expression add(Term first, Term second) {
        List<Term> terms = new ArrayList<Term>(2);
        terms.add(first);
        terms.add(second);
        return new Expression(terms);
    }

    public static Expression add(Term term, Variable variable) {
        return add(term, new Term(variable));
    }

    public static Expression add(Term term, double constant) {
        return new Expression(term, constant);
    }

    public static Expression subtract(Term term, Expression expression) {
        return add(negate(expression), term);
    }

    public static Expression subtract(Term first, Term second) {
        return add(first, negate(second));
    }

    public static Expression subtract(Term term, Variable variable) {
        return add(term, negate(variable));
    }

    public static Expression subtract(Term term, double constant) {
        return add(term, -constant);
    }

    // Variable add and subtract
    public static Expression add(Variable variable, Expression expression) {
        return add(expression, variable);
    }

    public static Expression add(Variable variable, Term term) {
        return add(term, variable);
    }

    public static Expression add(Variable first, Variable second) {
        return add(new Term(first), second);
    }

    public static Expression add(Variable variable, double constant) {
        return add(new Term(variable), constant);
    }

    public static Expression subtract(Variable variable, Expression expression) {
        return add(variable, negate(expression));
    }

    public static Expression subtract(Variable variable, Term term) {
        return add(variable, negate(term));
    }

    public static Expression subtract(Variable first, Variable second) {
        return add(first, negate(second));
    }

    public static Expression subtract(Variable variable, double constant) {
        return add(variable, -constant);
    }

    // Double add and subtract

    public static Expression add(double constant, Expression expression) {
        return add(expression, constant);
    }

    public static Expression add(double constant, Term term) {
        return add(term, constant);
    }

    public static Expression add(double constant, Variable variable) {
        return add(variable, constant);
    }

    public static Expression subtract(double constant, Expression expression) {
        return add(negate(expression), constant);
    }

    public static Expression subtract(double constant, Term term) {
        return add(negate(term), constant);
    }

    public static Expression subtract(double constant, Variable variable) {
        return add(negate(variable), constant);
    }

    // Expression relations
    public static Constraint equals(Expression first, Expression second) {
        return new Constraint(subtract(first, second), RelationalOperator.OP_EQ);
    }

    public static Constraint equals(Expression expression, Term term) {
        return equals(expression, new Expression(term));
    }

    public static Constraint equals(Expression expression, Variable variable) {
        return equals(expression, new Term(variable));
    }

    public static Constraint equals(Expression expression, double constant) {
        return equals(expression, new Expression(constant));
    }

    public static Constraint lessThanOrEqualTo(Expression first, Expression second) {
        return new Constraint(subtract(first, second), RelationalOperator.OP_LE);
    }

    public static Constraint lessThanOrEqualTo(Expression expression, Term term) {
        return lessThanOrEqualTo(expression, new Expression(term));
    }

    public static Constraint lessThanOrEqualTo(Expression expression, Variable variable) {
        return lessThanOrEqualTo(expression, new Term(variable));
    }

    public static Constraint lessThanOrEqualTo(Expression expression, double constant) {
        return lessThanOrEqualTo(expression, new Expression(constant));
    }

    public static Constraint greaterThanOrEqualTo(Expression first, Expression second) {
        return new Constraint(subtract(first, second), RelationalOperator.OP_GE);
    }

    public static Constraint greaterThanOrEqualTo(Expression expression, Term term) {
        return greaterThanOrEqualTo(expression, new Expression(term));
    }

    public static Constraint greaterThanOrEqualTo(Expression expression, Variable variable) {
        return greaterThanOrEqualTo(expression, new Term(variable));
    }

    public static Constraint greaterThanOrEqualTo(Expression expression, double constant) {
        return greaterThanOrEqualTo(expression, new Expression(constant));
    }

    // Term relations
    public static Constraint equals(Term term, Expression expression) {
        return equals(expression, term);
    }

    public static Constraint equals(Term first, Term second) {
        return equals(new Expression(first), second);
    }

    public static Constraint equals(Term term, Variable variable) {
        return equals(new Expression(term), variable);
    }

    public static Constraint equals(Term term, double constant) {
        return equals(new Expression(term), constant);
    }

    public static Constraint lessThanOrEqualTo(Term term, Expression expression) {
        return lessThanOrEqualTo(new Expression(term), expression);
    }

    public static Constraint lessThanOrEqualTo(Term first, Term second) {
        return lessThanOrEqualTo(new Expression(first), second);
    }

    public static Constraint lessThanOrEqualTo(Term term, Variable variable) {
        return lessThanOrEqualTo(new Expression(term), variable);
    }

    public static Constraint lessThanOrEqualTo(Term term, double constant) {
        return lessThanOrEqualTo(new Expression(term), constant);
    }

    public static Constraint greaterThanOrEqualTo(Term term, Expression expression) {
        return greaterThanOrEqualTo(new Expression(term), expression);
    }

    public static Constraint greaterThanOrEqualTo(Term first, Term second) {
        return greaterThanOrEqualTo(new Expression(first), second);
    }

    public static Constraint greaterThanOrEqualTo(Term term, Variable variable) {
        return greaterThanOrEqualTo(new Expression(term), variable);
    }

    public static Constraint greaterThanOrEqualTo(Term term, double constant) {
        return greaterThanOrEqualTo(new Expression(term), constant);
    }

    // Variable relations
    public static Constraint equals(Variable variable, Expression expression) {
        return equals(expression, variable);
    }

    public static Constraint equals(Variable variable, Term term) {
        return equals(term, variable);
    }

    public static Constraint equals(Variable first, Variable second) {
        return equals(new Term(first), second);
    }

    public static Constraint equals(Variable variable, double constant) {
        return equals(new Term(variable), constant);
    }

    public static Constraint lessThanOrEqualTo(Variable variable, Expression expression) {
        return lessThanOrEqualTo(new Term(variable), expression);
    }

    public static Constraint lessThanOrEqualTo(Variable variable, Term term) {
        return lessThanOrEqualTo(new Term(variable), term);
    }

    public static Constraint lessThanOrEqualTo(Variable first, Variable second) {
        return lessThanOrEqualTo(new Term(first), second);
    }

    public static Constraint lessThanOrEqualTo(Variable variable, double constant) {
        return lessThanOrEqualTo(new Term(variable), constant);
    }

    public static Constraint greaterThanOrEqualTo(Variable variable, Expression expression) {
        return greaterThanOrEqualTo(new Term(variable), expression);
    }

    public static Constraint greaterThanOrEqualTo(Variable variable, Term term) {
        return greaterThanOrEqualTo(term, variable);
    }

    public static Constraint greaterThanOrEqualTo(Variable first, Variable second) {
        return greaterThanOrEqualTo(new Term(first), second);
    }

    public static Constraint greaterThanOrEqualTo(Variable variable, double constant) {
        return greaterThanOrEqualTo(new Term(variable), constant);
    }

    // Double relations
    public static Constraint equals(double constant, Expression expression) {
        return equals(expression, constant);
    }

    public static Constraint equals(double constant, Term term) {
        return equals(term, constant);
    }

    public static Constraint equals(double constant, Variable variable) {
        return equals(variable, constant);
    }

    public static Constraint lessThanOrEqualTo(double constant, Expression expression) {
        return lessThanOrEqualTo(new Expression(constant), expression);
    }

    public static Constraint lessThanOrEqualTo(double constant, Term term) {
        return lessThanOrEqualTo(constant, new Expression(term));
    }

    public static Constraint lessThanOrEqualTo(double constant, Variable variable) {
        return lessThanOrEqualTo(constant, new Term(variable));
    }

    public static Constraint greaterThanOrEqualTo(double constant, Term term) {
        return greaterThanOrEqualTo(new Expression(constant), term);
    }

    public static Constraint greaterThanOrEqualTo(double constant, Variable variable) {
        return greaterThanOrEqualTo(constant, new Term(variable));
    }

    // Constraint strength modifier
    public static Constraint modifyStrength(Constraint constraint, double strength) {
        return new Constraint(constraint, strength);
    }

    public static Constraint modifyStrength(double strength, Constraint constraint) {
        return modifyStrength(strength, constraint);
    }

}
