package no.birkett.kiwi;

/**
 * Created by alex on 30/01/15.
 */
public class UnsatisfiableConstraintException extends KiwiException {

    private Constraint constraint;
    public UnsatisfiableConstraintException(Constraint constraint) {
        super(constraint.toString());
        this.constraint = constraint;
    }
}
