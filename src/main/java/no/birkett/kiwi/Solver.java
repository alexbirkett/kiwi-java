package no.birkett.kiwi;


import java.util.*;

/**
 * Created by alex on 30/01/15.
 */
public class Solver {

    private static class Tag {
        Symbol marker;
        Symbol other;

        public Tag(){
            marker = new Symbol();
            other = new Symbol();
        }
    }

    private static class EditInfo {
        Tag tag;
        Constraint constraint;
        double constant;

        public EditInfo(Constraint constraint, Tag tag, double constant){
            this.constraint = constraint;
            this.tag = tag;
            this.constant = constant;
        }
    }

    private Map<Constraint, Tag> cns = new LinkedHashMap<Constraint, Tag>();
    private Map<Symbol, Row> rows = new LinkedHashMap<Symbol, Row>();
    private Map<Variable, Symbol> vars = new LinkedHashMap<Variable, Symbol>();
    private Map<Variable, EditInfo> edits = new LinkedHashMap<Variable, EditInfo>();
    private List<Symbol> infeasibleRows = new ArrayList<Symbol>();
    private Row objective = new Row();
    private Row artificial;


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

        Tag tag = new Tag();
        Row row = createRow(constraint, tag);
        Symbol subject = chooseSubject(row, tag);

        if(subject.getType() == Symbol.Type.INVALID && allDummies(row)){
            if (!Util.nearZero(row.getConstant())) {
                throw new UnsatisfiableConstraintException(constraint);
            } else {
                subject = tag.marker;
            }
        }

        if (subject.getType() == Symbol.Type.INVALID) {
            if (!addWithArtificialVariable(row)) {
                throw new UnsatisfiableConstraintException(constraint);
            }
        } else {
            row.solveFor(subject);
            substitute(subject, row);
            this.rows.put(subject, row);
        }

        this.cns.put(constraint, tag);

        optimize(objective);
    }

    public void removeConstraint(Constraint constraint) throws UnknownConstraintException, InternalSolverError{
        Tag tag = cns.get(constraint);
        if(tag == null){
            throw new UnknownConstraintException(constraint);
        }

        cns.remove(constraint);
        removeConstraintEffects(constraint, tag);

        Row row = rows.get(tag.marker);
        if(row != null){
            rows.remove(tag.marker);
        }
        else{
            row = getMarkerLeavingRow(tag.marker);
            if(row == null){
                throw new InternalSolverError("internal solver error");
            }

            //This looks wrong! changes made below
            //Symbol leaving = tag.marker;
            //rows.remove(tag.marker);

            Symbol leaving = null;
            for(Symbol s: rows.keySet()){
                if(rows.get(s) == row){
                    leaving = s;
                }
            }
            if(leaving == null){
                throw new InternalSolverError("internal solver error");
            }

            rows.remove(leaving);
            row.solveFor(leaving, tag.marker);
            substitute(tag.marker, row);
        }
        optimize(objective);
    }

    void removeConstraintEffects(Constraint constraint, Tag tag){
        if(tag.marker.getType() == Symbol.Type.ERROR){
            removeMarkerEffects(tag.marker, constraint.getStrength());
        }
        else if(tag.other.getType() == Symbol.Type.ERROR){
            removeMarkerEffects(tag.other, constraint.getStrength());
        }
    }

    void removeMarkerEffects(Symbol marker, double strength){
        Row row = rows.get(marker);
        if(row != null){
            objective.insert(row, -strength);
        }else {
            objective.insert(marker, -strength);
        }
    }

    Row getMarkerLeavingRow(Symbol marker){
        double dmax = Double.MAX_VALUE;
        double r1 = dmax;
        double r2 = dmax;

        Row first = null;
        Row second = null;
        Row third = null;

        for(Symbol s: rows.keySet()){
            Row candidateRow = rows.get(s);
            double c = candidateRow.coefficientFor(marker);
            if(c == 0.0){
                continue;
            }
            if(s.getType() == Symbol.Type.EXTERNAL){
                third = candidateRow;
            }
            else if(c < 0.0){
                double r = - candidateRow.getConstant() / c;
                if(r < r1){
                    r1 = r;
                    first = candidateRow;
                }
            }
            else{
                double r = candidateRow.getConstant() / c;
                if(r < r2){
                    r2 = r;
                    second = candidateRow;
                }
            }
        }

        if(first != null){
            return first;
        }
        if(second != null){
            return second;
        }
        return third;
    }

    public boolean hasConstraint(Constraint constraint){
        return cns.containsKey(constraint);
    }

    public void addEditVariable(Variable variable, double strength) throws DuplicateEditVariableException, RequiredFailureException{
        if(edits.containsKey(variable)){
            throw new DuplicateEditVariableException();
        }

        strength = Strength.clip(strength);

        if(strength == Strength.REQUIRED){
            throw new RequiredFailureException();
        }

        List<Term> terms = new ArrayList<>();
        terms.add(new Term(variable));
        Constraint constraint = new Constraint(new Expression(terms), RelationalOperator.OP_EQ, strength);

        try {
            addConstraint(constraint);
        } catch (DuplicateConstraintException e) {
            e.printStackTrace();
        } catch (UnsatisfiableConstraintException e) {
            e.printStackTrace();
        }


        EditInfo info = new EditInfo(constraint, cns.get(constraint), 0.0);
        edits.put(variable, info);
    }

    public void removeEditVariable(Variable variable) throws UnknownEditVariableException{
        EditInfo edit = edits.get(variable);
        if(edit == null){
            throw new UnknownEditVariableException();
        }

        try {
            removeConstraint(edit.constraint);
        } catch (UnknownConstraintException e) {
            e.printStackTrace();
        }

        edits.remove(variable);
    }

    public boolean hasEditVariable(Variable variable){
        return edits.containsKey(variable);
    }

    public void suggestValue(Variable variable, double value) throws UnknownEditVariableException{
        EditInfo info = edits.get(variable);
        if(info == null){
            throw new UnknownEditVariableException();
        }

        double delta = value - info.constant;
        info.constant = value;

        Row row = rows.get(info.tag.marker);
        if(row != null){
            if(row.add(-delta) < 0.0){
                infeasibleRows.add(info.tag.marker);
            }
			dualOptimize();
            return;
        }

        row = rows.get(info.tag.other);
        if(row != null){
            if(row.add(delta) < 0.0){
                infeasibleRows.add(info.tag.other);
            }
			dualOptimize();
            return;
        }

        for(Symbol s: rows.keySet()){
            Row currentRow = rows.get(s);
            double coefficient = currentRow.coefficientFor(info.tag.marker);
            if(coefficient != 0.0 && currentRow.add(delta * coefficient) < 0.0 && s.getType() != Symbol.Type.EXTERNAL){
                infeasibleRows.add(s);
            }
        }

        dualOptimize();
    }

    /**
     * Update the values of the external solver variables.
     */
    public void updateVariables() {

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
    Row createRow(Constraint constraint, Tag tag) {
        Expression expression = constraint.getExpression();
        Row row = new Row(expression.getConstant());

        for (Term term : expression.getTerms()) {
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

        switch (constraint.getOp()) {
            case OP_LE:
            case OP_GE: {
                double coeff = constraint.getOp() == RelationalOperator.OP_LE ? 1.0 : -1.0;
                Symbol slack = new Symbol(Symbol.Type.SLACK);
                tag.marker = slack;
                row.insert(slack, coeff);
                if (constraint.getStrength() < Strength.REQUIRED) {
                    Symbol error = new Symbol(Symbol.Type.ERROR);
                    tag.other = error;
                    row.insert(error, -coeff);
                    this.objective.insert(error, constraint.getStrength());
                }
                break;
            }
            case OP_EQ: {
                if (constraint.getStrength() < Strength.REQUIRED) {
                    Symbol errplus = new Symbol(Symbol.Type.ERROR);
                    Symbol errminus = new Symbol(Symbol.Type.ERROR);
                    tag.marker = errplus;
                    tag.other = errminus;
                    row.insert(errplus, -1.0); // v = eplus - eminus
                    row.insert(errminus, 1.0); // v - eplus + eminus = 0
                    this.objective.insert(errplus, constraint.getStrength());
                    this.objective.insert(errminus, constraint.getStrength());
                } else {
                    Symbol dummy = new Symbol(Symbol.Type.DUMMY);
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

        return row;
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

        Symbol art = new Symbol(Symbol.Type.SLACK);
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

            /**this looks wrong!!!*/
            //rows.remove(rowptr);

            LinkedList<Symbol> deleteQueue = new LinkedList<>();
            for(Symbol s: rows.keySet()){
                if(rows.get(s) == rowptr){
                    deleteQueue.add(s);
                }
            }
            while(!deleteQueue.isEmpty()){
                rows.remove(deleteQueue.pop());
            }
            deleteQueue.clear();

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

            Row entry = getLeavingRow(entering);
            if(entry == null){
                throw  new InternalSolverError("The objective is unbounded.");
            }
            Symbol leaving = null;

            for(Symbol key: rows.keySet()){
                if(rows.get(key) == entry){
                    leaving = key;
                }
            }

            Symbol entryKey = null;
            for(Symbol key: rows.keySet()){
                if(rows.get(key) == entry){
                    entryKey = key;
                }
            }

            rows.remove(entryKey);
            entry.solveFor(leaving, entering);
            substitute(entering, entry);
            rows.put(entering, entry);
        }
    }

    void dualOptimize() throws InternalSolverError{
        while(!infeasibleRows.isEmpty()){
            Symbol leaving = infeasibleRows.remove(infeasibleRows.size() - 1);
            Row row = rows.get(leaving);
            if(row != null && row.getConstant() < 0.0){
                Symbol entering = getDualEnteringSymbol(row);
                if(entering.getType() == Symbol.Type.INVALID){
                    throw new InternalSolverError("internal solver error");
                }
                rows.remove(leaving);
                row.solveFor(leaving, entering);
                substitute(entering, row);
                rows.put(entering, row);
            }
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

    private Symbol getDualEnteringSymbol(Row row){
        Symbol entering = new Symbol();
        double ratio = Double.MAX_VALUE;
        for(Symbol s: row.getCells().keySet()){
            if(s.getType() != Symbol.Type.DUMMY){
                double currentCell = row.getCells().get(s);
                if(currentCell > 0.0){
                    double coefficient = objective.coefficientFor(s);
                    double r = coefficient / currentCell;
                    if(r < ratio){
                        ratio = r;
                        entering = s;
                    }
                }
            }
        }
        return entering;
    }


    /**
     * Get the first Slack or Error symbol in the row.
     * <p/>
     * If no such symbol is present, and Invalid symbol will be returned.
     */
    private Symbol anyPivotableSymbol(Row row) {
        Symbol symbol = null;
        for (Map.Entry<Symbol, Double> entry : row.getCells().entrySet()) {
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
    private Row getLeavingRow(Symbol entering) {
        double ratio = Double.MAX_VALUE;
        Row row = null;

        for(Symbol key: rows.keySet()){
            if(key.getType() != Symbol.Type.EXTERNAL){
                Row candidateRow = rows.get(key);
                double temp = candidateRow.coefficientFor(entering);
                if(temp < 0){
                    double temp_ratio = (-candidateRow.getConstant() / temp);
                    if(temp_ratio < ratio){
                        ratio = temp_ratio;
                        row = candidateRow;
                    }
                }
            }
        }
        return row;
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
            symbol = new Symbol(Symbol.Type.EXTERNAL);
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
