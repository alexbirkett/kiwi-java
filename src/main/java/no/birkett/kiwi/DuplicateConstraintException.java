package no.birkett.kiwi;

/**
 * Created by alex on 30/01/15.
 */
public class DuplicateConstraintException extends KiwiException {

    private Constraint constraint;

    public DuplicateConstraintException(Constraint constraint) {
        this.constraint = constraint;
    }
}
