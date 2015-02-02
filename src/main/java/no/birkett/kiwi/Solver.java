package no.birkett.kiwi;


import java.util.*;

/**
 * Created by alex on 30/01/15.
 */
public class Solver {

    private static class Tag {
        Symbol marker;
        Symbol other;
    }

    private static class EditInfo {
        Tag tag;
        Constraint constraint;
        double constant;
    }

    private static class RowAndTag {
        Tag tag;
        Row row;
    }

    private Map<Constraint, Tag> cns = new HashMap<Constraint, Tag>();
    private Map<Symbol, Row> rows = new HashMap<Symbol, Row>();
    private Map<Variable, Symbol> vars = new HashMap<Variable, Symbol>();
    private Map<Variable, EditInfo> edits = new HashMap<Variable, EditInfo>();
    private List<Symbol> infeasibleRows = new ArrayList<Symbol>();
    private Row objective = new Row();
    private Row artificial;
    private long idTick = 1;


    /**
     * Add a constraint to the solver.
     *
     * @param constraint
     * @throws DuplicateConstraintException The given constraint has already been added to the solver.
     * @throws UnsatisfiableConstraintException      The given constraint is required and cannot be satisfied.
     */
    public void addConstraint(Constraint constraint) throws DuplicateConstraintException, UnsatisfiableConstraintException {

        if (cns.containsKey(constraint)) {
            throw new DuplicateConstraintException(constraint);
        }

        // Creating a row causes symbols to reserved for the variables
        // in the constraint. If this method exits with an exception,
        // then its possible those variables will linger in the var map.
        // Since its likely that those variables will be used in other
        // constraints and since exceptional conditions are uncommon,
        // i'm not too worried about aggressive cleanup of the var map.

        RowAndTag rowAndTag = createRow(constraint);

        Symbol subject = chooseSubject(rowAndTag.row, rowAndTag.tag);

        // If chooseSubject could find a valid entering symbol, one
        // last option is available if the entire row is composed of
        // dummy variables. If the constant of the row is zero, then
        // this represents redundant constraints and the new dummy
        // marker can enter the basis. If the constant is non-zero,
        // then it represents an unsatisfiable constraint.
        if (subject.getType() == Symbol.Type.INVALID && allDummies(rowAndTag.row)) {
            if (!Util.nearZero(rowAndTag.row.getConstant())) {
                throw new UnsatisfiableConstraintException(constraint);
            } else {
                subject = rowAndTag.tag.marker;
            }
        }

        // If an entering symbol still isn't found, then the row must
        // be added using an artificial variable. If that fails, then
        // the row represents an unsatisfiable constraint.
        if (subject.getType() == Symbol.Type.INVALID) {
            if (!addWithArtificialVariable(rowAndTag.row)) {
                throw new UnsatisfiableConstraintException(constraint);
            }
        } else {
            rowAndTag.row.solveFor(subject);
            substitute(subject, rowAndTag.row);
            this.rows.put(subject, rowAndTag.row);
        }

        this.cns.put(constraint, rowAndTag.tag);

        // Optimizing after each constraint is added performs less
        // aggregate work due to a smaller average system size. It
        // also ensures the solver remains in a consistent state.
        optimize(this.objective);
    }

    /**
     * Update the values of the external solver variables.
     */
    void updateVariables() {

        for (Map.Entry<Variable, Symbol> varEntry : vars.entrySet()) {
            Variable variable = varEntry.getKey();
            Row row = this.rows.get(varEntry.getValue());

            if (row == null) {
                variable.setValue(0);
            } else {
                variable.setValue(row.getConstant());
            }
        }
    }


    /**
     * Create a new Row object for the given constraint.
     * <p/>
     * The terms in the constraint will be converted to cells in the row.
     * Any term in the constraint with a coefficient of zero is ignored.
     * This method uses the `getVarSymbol` method to get the symbol for
     * the variables added to the row. If the symbol for a given cell
     * variable is basic, the cell variable will be substituted with the
     * basic row.
     * <p/>
     * The necessary slack and error variables will be added to the row.
     * If the constant for the row is negative, the sign for the row
     * will be inverted so the constant becomes positive.
     * <p/>
     * The tag will be updated with the marker and error symbols to use
     * for tracking the movement of the constraint in the tableau.
     */
    RowAndTag createRow(Constraint constraint) {

        Expression expr = constraint.getExpression();
        Row row = new Row(expr.getConstant());
        Tag tag = new Tag();

        // Substitute the current basic variables into the row.
        for (Term term : expr.getTerms()) {
            if (!Util.nearZero(term.getCoefficient())) {
                Symbol symbol = getVarSymbol(term.getVariable());

                Row otherRow = rows.get(symbol);

                if (otherRow == null) {
                    row.insert(symbol, term.getCoefficient());
                } else {
                    row.insert(otherRow, term.getCoefficient());
                }
            }
        }

        // Add the necessary slack, error, and dummy variables.
        switch (constraint.getOp()) {
            case OP_LE:
            case OP_GE: {
                double coeff = constraint.getOp() == RelationalOperator.OP_LE ? 1.0 : -1.0;
                Symbol slack = new Symbol(Symbol.Type.SLACK, idTick++);
                tag.marker = slack;
                row.insert(slack, coeff);
                if (constraint.getStrength() < Strength.REQUIRED) {
                    Symbol error = new Symbol(Symbol.Type.ERROR, idTick++);
                    tag.other = error;
                    row.insert(error, -coeff);
                    this.objective.insert(error, constraint.getStrength());
                }
                break;
            }
            case OP_EQ: {
                if (constraint.getStrength() < Strength.REQUIRED) {
                    Symbol errplus = new Symbol(Symbol.Type.ERROR, idTick++);
                    Symbol errminus = new Symbol(Symbol.Type.ERROR, idTick++);
                    tag.marker = errplus;
                    tag.other = errminus;
                    row.insert(errplus, -1.0); // v = eplus - eminus
                    row.insert(errminus, 1.0); // v - eplus + eminus = 0
                    this.objective.insert(errplus, constraint.getStrength());
                    this.objective.insert(errminus, constraint.getStrength());
                } else {
                    Symbol dummy = new Symbol(Symbol.Type.DUMMY, idTick++);
                    tag.marker = dummy;
                    row.insert(dummy);
                }
                break;
            }
        }

        // Ensure the row as a positive constant.
        if (row.getConstant() < 0.0) {
            row.reverseSign();
        }

        RowAndTag rowAndTag = new RowAndTag();
        rowAndTag.row = row;
        rowAndTag.tag = tag;


        return rowAndTag;
    }

    /**
     * Choose the subject for solving for the row
     * <p/>
     * This method will choose the best subject for using as the solve
     * target for the row. An invalid symbol will be returned if there
     * is no valid target.
     * The symbols are chosen according to the following precedence:
     * 1) The first symbol representing an external variable.
     * 2) A negative slack or error tag variable.
     * If a subject cannot be found, an invalid symbol will be returned.
     */
    private static Symbol chooseSubject(Row row, Tag tag) {

        for (Map.Entry<Symbol, Double> cell : row.getCells().entrySet()) {
            if (cell.getKey().getType() == Symbol.Type.EXTERNAL) {
                return cell.getKey();
            }
        }
        if (tag.marker.getType() == Symbol.Type.SLACK || tag.marker.getType() == Symbol.Type.ERROR) {
            if (row.coefficientFor(tag.marker) < 0.0)
                return tag.marker;
        }
        if (tag.other != null && (tag.other.getType() == Symbol.Type.SLACK || tag.other.getType() == Symbol.Type.ERROR)) {
            if (row.coefficientFor(tag.other) < 0.0)
                return tag.other;
        }
        return new Symbol();
    }

    /**
     * Add the row to the tableau using an artificial variable.
     * <p/>
     * This will return false if the constraint cannot be satisfied.
     */
    private boolean addWithArtificialVariable(Row row) {
        //TODO check this

        // Create and add the artificial variable to the tableau

        Symbol art = new Symbol(Symbol.Type.SLACK, idTick++);
        rows.put(art, new Row(row));

        this.artificial = new Row(row);

        // Optimize the artificial objective. This is successful
        // only if the artificial objective is optimized to zero.
        optimize(this.artificial);
        boolean success = Util.nearZero(artificial.getConstant());
        artificial = null;

        // If the artificial variable is basic, pivot the row so that
        // it becomes basic. If the row is constant, exit early.

        Row rowptr = this.rows.get(art);

        if (rowptr != null) {
            rows.remove(rowptr);
            if (rowptr.getCells().isEmpty()) {
                return success;
            }
            Symbol entering = anyPivotableSymbol(rowptr);
            if (entering.getType() == Symbol.Type.INVALID) {
                return false; // unsatisfiable (will this ever happen?)
            }
            rowptr.solveFor(art, entering);
            substitute(entering, rowptr);
            this.rows.put(entering, rowptr);
        }

        // Remove the artificial variable from the tableau.
        for (Map.Entry<Symbol, Row> rowEntry : rows.entrySet()) {
            rowEntry.getValue().remove(art);
        }

        objective.remove(art);

        return success;
    }

    /**
     * Substitute the parametric symbol with the given row.
     * <p/>
     * This method will substitute all instances of the parametric symbol
     * in the tableau and the objective function with the given row.
     */
    void substitute(Symbol symbol, Row row) {
        for (Map.Entry<Symbol, Row> rowEntry : rows.entrySet()) {
            rowEntry.getValue().substitute(symbol, row);
            if (rowEntry.getKey().getType() != Symbol.Type.EXTERNAL && rowEntry.getValue().getConstant() < 0.0) {
                infeasibleRows.add(rowEntry.getKey());
            }
        }

        objective.substitute(symbol, row);

        if (artificial != null) {
            artificial.substitute(symbol, row);
        }
    }

    /**
     * Optimize the system for the given objective function.
     * <p/>
     * This method performs iterations of Phase 2 of the simplex method
     * until the objective function reaches a minimum.
     *
     * @throws InternalSolverError The value of the objective function is unbounded.
     */
    void optimize(Row objective) {
        while (true) {
            Symbol entering = getEnteringSymbol(objective);
            if (entering.getType() == Symbol.Type.INVALID) {
                return;
            }

            Map.Entry<Symbol, Row> entry = getLeavingRow(entering);

            if (entry == null) {
                throw new InternalSolverError("The objective is unbounded.");
            }

            // pivot the entering symbol into the basis
            Symbol leaving = entry.getKey();
            Row row = entry.getValue();

            this.rows.remove(entry.getKey());

            row.solveFor(leaving, entering);

            substitute(entering, row);

            this.rows.put(entering, row);
        }
    }


    /**
     * Compute the entering variable for a pivot operation.
     * <p/>
     * This method will return first symbol in the objective function which
     * is non-dummy and has a coefficient less than zero. If no symbol meets
     * the criteria, it means the objective function is at a minimum, and an
     * invalid symbol is returned.
     */
    private static Symbol getEnteringSymbol(Row objective) {

        for (Map.Entry<Symbol, Double> cell : objective.getCells().entrySet()) {

            if (cell.getKey().getType() != Symbol.Type.DUMMY && cell.getValue() < 0.0) {
                return cell.getKey();
            }
        }
        return new Symbol();

    }

    /**
     * Get the first Slack or Error symbol in the row.
     * <p/>
     * If no such symbol is present, and Invalid symbol will be returned.
     */
    private Symbol anyPivotableSymbol(Row row) {
        Symbol symbol = null;
        for (Map.Entry<Symbol, Double> entry : objective.getCells().entrySet()) {
            if (entry.getKey().getType() == Symbol.Type.SLACK || entry.getKey().getType() == Symbol.Type.ERROR) {
                symbol = entry.getKey();
            }
        }
        if (symbol == null) {
            symbol = new Symbol();
        }
        return symbol;
    }

    /**
     * Compute the row which holds the exit symbol for a pivot.
     * <p/>
     * This documentation is copied from the C++ version and is outdated
     * <p/>
     * <p/>
     * This method will return an iterator to the row in the row map
     * which holds the exit symbol. If no appropriate exit symbol is
     * found, the end() iterator will be returned. This indicates that
     * the objective function is unbounded.
     */
    private Map.Entry<Symbol, Row> getLeavingRow(Symbol entering) {
        // TODO check
        double ratio = Double.MAX_VALUE;
        Map.Entry<Symbol, Row> found = null;
        for (Map.Entry<Symbol, Row> row : rows.entrySet()) {
            if (row.getKey().getType() != Symbol.Type.EXTERNAL) {
                double temp = row.getValue().coefficientFor(entering);
                if (temp < 0.0) {
                    double temp_ratio = -row.getValue().getConstant() / temp;
                    if (temp_ratio < ratio) {
                        ratio = temp_ratio;
                        found = row;
                    }
                }
            }
        }
        return found;
    }

    /**
     * Get the symbol for the given variable.
     * <p/>
     * If a symbol does not exist for the variable, one will be created.
     */
    private Symbol getVarSymbol(Variable variable) {
        Symbol symbol;
        if (vars.containsKey(variable)) {
            symbol = vars.get(variable);
        } else {
            symbol = new Symbol(Symbol.Type.EXTERNAL, idTick++);
            vars.put(variable, symbol);
        }
        return symbol;
    }

    /**
     * Test whether a row is composed of all dummy variables.
     */
    private static boolean allDummies(Row row) {
        for (Map.Entry<Symbol, Double> cell : row.getCells().entrySet()) {
            if (cell.getKey().getType() != Symbol.Type.DUMMY) {
                return false;
            }
        }
        return true;
    }

}
