package no.birkett.kiwi;

import java.util.HashMap;

/**
 * Created by alex on 27/11/2014.
 */
public class Benchmarks {

    public static void testAddingLotsOfConstraints() throws DuplicateConstraintException, UnsatisfiableConstraintException, NonlinearExpressionException {
        Solver solver = new Solver();

        final HashMap<String, Variable> variables = new HashMap<String, Variable>();

        ConstraintParser.CassowaryVariableResolver variableResolver = new ConstraintParser.CassowaryVariableResolver() {

            @Override
            public Variable resolveVariable(String variableName) {
                Variable variable = null;
                if (variables.containsKey(variableName)) {
                    variable =  variables.get(variableName);
                } else {
                    variable = new Variable(variableName);
                    variables.put(variableName, variable);
                }
                return variable;
            }

            @Override
            public Expression resolveConstant(String name) {
                try {
                    return new Expression(Double.parseDouble(name));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        };

        solver.addConstraint(ConstraintParser.parseConstraint("variable0 == 100", variableResolver));

        for (int i = 1; i < 3000; i++) {
            String constraintString  = getVariableName(i) + " == 100 + " + getVariableName(i - 1);

            Constraint constraint = ConstraintParser.parseConstraint(constraintString, variableResolver);

            System.gc();
            long timeBefore = System.nanoTime();

            solver.addConstraint(constraint);

            System.out.println(i + "," + ((System.nanoTime() - timeBefore) / 1000) );
        }


    }

    private static String getVariableName(int number) {
        return "getVariable" + number;
    }

    public static void main(String [ ] args) {
        try {
            testAddingLotsOfConstraints();
        } catch (DuplicateConstraintException e) {
            e.printStackTrace();
        } catch (UnsatisfiableConstraintException e) {
            e.printStackTrace();
        } catch (NonlinearExpressionException e) {
            e.printStackTrace();
        }
    }

}
