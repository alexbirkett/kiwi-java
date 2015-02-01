package no.birkett.kiwi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class Tests {

    private static double EPSILON = 1.0e-8;

    @Test
    public void simpleNew() throws UnsatisfiableConstraintException, DuplicateConstraintException {
        Solver solver = new Solver();
        Variable x = new Variable("x");


        solver.addConstraint(Symbolics.equals(Symbolics.add(x,2), 20));

        solver.updateVariables();

        assertEquals(x.getValue(), 18, EPSILON);
    }
    
    @Test
    public void simple0() throws UnsatisfiableConstraintException, DuplicateConstraintException {
        Solver solver = new Solver();
        Variable x = new Variable("x");
        Variable y = new Variable("y");

        // x = 20
        solver.addConstraint(Symbolics.equals(x, 20));

        // x + 2 == y + 10
        //
        solver.addConstraint(Symbolics.equals(Symbolics.add(x,2), Symbolics.add(y, 10)));

        solver.updateVariables();
        
        System.out.println("x " + x.getValue() + " y " + y.getValue());

        assertEquals(y.getValue(), 12, EPSILON);
        assertEquals(x.getValue(), 20, EPSILON);
    }
    
    @Test
    public void simple1() throws DuplicateConstraintException, UnsatisfiableConstraintException {
        Variable x = new Variable("x");
        Variable y = new Variable("y");
        Solver solver = new Solver();
        solver.addConstraint(Symbolics.equals(x, y));
        solver.updateVariables();
        assertEquals(x.getValue(), y.getValue(), EPSILON);
    }

    @Test
    public void casso1() throws DuplicateConstraintException, UnsatisfiableConstraintException {
        Variable x = new Variable("x");
        Variable y = new Variable("y");
        Solver solver = new Solver();

        solver.addConstraint(Symbolics.lessThanOrEqualTo(x, y));
        solver.addConstraint(Symbolics.equals(y, Symbolics.add(x, 3.0)));
        solver.addConstraint(Symbolics.equals(x, 10.0).setStrength(Strength.WEAK));
        solver.addConstraint(Symbolics.equals(y, 10.0).setStrength(Strength.WEAK));

        solver.updateVariables();
        
        if (Math.abs(x.getValue() - 10.0) < EPSILON) {
            assertEquals(10, x.getValue(), EPSILON);
            assertEquals(13, y.getValue(), EPSILON);
        } else {
            assertEquals(7, x.getValue(), EPSILON);
            assertEquals(10, y.getValue(), EPSILON);
        }
    }


    /*@Test
    public void addDelete1() {
        Variable x = new Variable("x");
        Solver solver = new Solver();

        solver.addConstraint(new Constraint(x, Constraint.Operator.EQ, 100, Strength.WEAK));

        Constraint c10 = new Constraint(x, Constraint.Operator.LEQ, 10.0);
        Constraint c20 = new Constraint(x, Constraint.Operator.LEQ, 20.0);

        solver.addConstraint(c10);
        solver.addConstraint(c20);

        assertEquals(10, x.value(), EPSILON);

        solver.removeConstraint(c10);
        assertEquals(20, x.value(), EPSILON);

        solver.removeConstraint(c20);
        assertEquals(100, x.value(), EPSILON);

        Constraint c10again = new Constraint(x, Constraint.Operator.LEQ, 10.0);

        solver.addConstraint(c10);
        solver.addConstraint(c10again);

        assertEquals(10, x.value(), EPSILON);

        solver.removeConstraint(c10);
        assertEquals(10, x.value(), EPSILON);

        solver.removeConstraint(c10again);
        assertEquals(100, x.value(), EPSILON);
    }


    @Test
    public void addDelete2() {
        Variable x = new Variable("x");
        Variable y = new Variable("y");
        Solver solver = new Solver();

        solver.addConstraint(new Constraint(x, Constraint.Operator.EQ, 100.0, Strength.WEAK));
        solver.addConstraint(new Constraint(y, Constraint.Operator.EQ, 120.0, Strength.STRONG));


        Constraint c10 = new Constraint(x, Constraint.Operator.LEQ, 10.0);
        Constraint c20 = new Constraint(x, Constraint.Operator.LEQ, 20.0);

        solver.addConstraint(c10);
        solver.addConstraint(c20);

        assertEquals(10, x.value(), EPSILON);
        assertEquals(120, y.value(), EPSILON);

        solver.removeConstraint(c10);
        assertEquals(20, x.value(), EPSILON);
        assertEquals(120, y.value(), EPSILON);

        Constraint cxy = new Constraint(x.times(2.0), Constraint.Operator.EQ, y);
        solver.addConstraint(cxy);
        assertEquals(20, x.value(), EPSILON);
        assertEquals(40, y.value(), EPSILON);

        solver.removeConstraint(c20);
        assertEquals(60, x.value(), EPSILON);
        assertEquals(120, y.value(), EPSILON);

        solver.removeConstraint(cxy);
        assertEquals(100, x.value(), EPSILON);
        assertEquals(120, y.value(), EPSILON);
    }



    @Test(expected = RequiredFailure.class)
    public void inconsistent1() throws InternalError {
        Variable x = new Variable("x");
        Solver solver = new Solver();

        solver.addConstraint(new Constraint(x, Constraint.Operator.EQ, 10.0));
        solver.addConstraint(new Constraint(x, Constraint.Operator.EQ, 5.0));
    }


    @Test(expected = RequiredFailure.class)
    public void inconsistent2() {
        Variable x = new Variable("x");
        Solver solver = new Solver();

        solver.addConstraint(new Constraint(x, Constraint.Operator.GEQ, 10.0));
        solver.addConstraint(new Constraint(x, Constraint.Operator.LEQ, 5.0));
    }



    @Test(expected = RequiredFailure.class)
    public void inconsistent3() {

        Variable w = new Variable("w");
        Variable x = new Variable("x");
        Variable y = new Variable("y");
        Variable z = new Variable("z");
        Solver solver = new Solver();

        solver.addConstraint(new Constraint(w, Constraint.Operator.GEQ, 10.0));
        solver.addConstraint(new Constraint(x, Constraint.Operator.GEQ, w));
        solver.addConstraint(new Constraint(y, Constraint.Operator.GEQ, x));
        solver.addConstraint(new Constraint(z, Constraint.Operator.GEQ, y));
        solver.addConstraint(new Constraint(z, Constraint.Operator.GEQ, 8.0));
        solver.addConstraint(new Constraint(z, Constraint.Operator.LEQ, 4.0));
    }*/

}
