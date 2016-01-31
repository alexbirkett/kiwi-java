package no.birkett.kiwi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 30/01/15.
 */
public class Expression {

    private List<Term> terms;

    private double constant;

    public Expression() {
        this(0);
    }

    public Expression(double constant) {
        this.constant = constant;
        this.terms = new ArrayList<Term>();
    }

    public Expression(Term term, double constant) {
        this.terms = new ArrayList<Term>();
        terms.add(term);
        this.constant = constant;
    }

    public Expression(Term term) {
        this (term, 0.0);
    }

    public Expression(List<Term> terms, double constant) {
        this.terms = terms;
        this.constant = constant;
    }

    public Expression(List<Term> terms) {
        this(terms, 0);
    }

    public double getConstant() {
        return constant;
    }

    public void setConstant(double constant) {
        this.constant = constant;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    public double getValue() {
        double result = this.constant;

        for (Term term : terms) {
            result += term.getValue();
        }
        return result;
    }

    public final boolean isConstant() {
        return terms.size() == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("isConstant: " + isConstant() + " constant: " + constant);
        if (!isConstant()) {
            sb.append(" terms: [");
            for (Term term: terms) {
                sb.append("(");
                sb.append(term);
                sb.append(")");
            }
            sb.append("] ");
        }
        return sb.toString();
    }

}

