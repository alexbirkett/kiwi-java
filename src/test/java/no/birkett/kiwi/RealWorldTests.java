package no.birkett.kiwi;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by alex on 20/11/2014.
 */
public class RealWorldTests {

    private static double EPSILON = 1.0e-2;

    public static final String LEFT = "left";
    public static final String RIGHT = "right";
    public static final String TOP = "top";
    public static final String BOTTOM = "bottom";
    public static final String HEIGHT = "height";
    public static final String WIDTH = "width";
    public static final String CENTERX = "centerX";
    public static final String CENTERY = "centerY";

    private static final String[] CONSTRAINTS = {

            "container.columnWidth == container.width * 0.4",
            "container.thumbHeight == container.columnWidth / 2",
            "container.padding == container.width * (0.2 / 3)",
            "container.leftPadding == container.padding",
            "container.rightPadding == container.width - container.padding",
            "container.paddingUnderThumb == 5",
            "container.rowPadding == 15",
            "container.buttonPadding == 20",

            "thumb0.left == container.leftPadding",
            "thumb0.top == container.padding",
            "thumb0.height == container.thumbHeight",
            "thumb0.width == container.columnWidth",

            "title0.left == container.leftPadding",
            "title0.top == thumb0.bottom + container.paddingUnderThumb",
            "title0.height == title0.intrinsicHeight",
            "title0.width == container.columnWidth",

            "thumb1.right == container.rightPadding",
            "thumb1.top == container.padding",
            "thumb1.height == container.thumbHeight",
            "thumb1.width == container.columnWidth",

            "title1.right == container.rightPadding",
            "title1.top == thumb0.bottom + container.paddingUnderThumb",
            "title1.height == title1.intrinsicHeight",
            "title1.width == container.columnWidth",

            "thumb2.left == container.leftPadding",
            "thumb2.top >= title0.bottom + container.rowPadding",
            "thumb2.top == title0.bottom + container.rowPadding !weak",
            "thumb2.top >= title1.bottom + container.rowPadding",
            "thumb2.top == title1.bottom + container.rowPadding !weak",
            "thumb2.height == container.thumbHeight",
            "thumb2.width == container.columnWidth",

            "title2.left == container.leftPadding",
            "title2.top == thumb2.bottom + container.paddingUnderThumb",
            "title2.height == title2.intrinsicHeight",
            "title2.width == container.columnWidth",

            "thumb3.right == container.rightPadding",
            "thumb3.top == thumb2.top",

            "thumb3.height == container.thumbHeight",
            "thumb3.width == container.columnWidth",

            "title3.right == container.rightPadding",
            "title3.top == thumb3.bottom + container.paddingUnderThumb",
            "title3.height == title3.intrinsicHeight",
            "title3.width == container.columnWidth",

            "thumb4.left == container.leftPadding",
            "thumb4.top >= title2.bottom + container.rowPadding",
            "thumb4.top >= title3.bottom + container.rowPadding",
            "thumb4.top == title2.bottom + container.rowPadding !weak",
            "thumb4.top == title3.bottom + container.rowPadding !weak",
            "thumb4.height == container.thumbHeight",
            "thumb4.width == container.columnWidth",

            "title4.left == container.leftPadding",
            "title4.top == thumb4.bottom + container.paddingUnderThumb",
            "title4.height == title4.intrinsicHeight",
            "title4.width == container.columnWidth",

            "thumb5.right == container.rightPadding",
            "thumb5.top == thumb4.top",
            "thumb5.height == container.thumbHeight",
            "thumb5.width == container.columnWidth",

            "title5.right == container.rightPadding",
            "title5.top == thumb5.bottom + container.paddingUnderThumb",
            "title5.height == title5.intrinsicHeight",
            "title5.width == container.columnWidth",

            "line.height == 1",
            "line.width == container.width",
            "line.top >= title4.bottom + container.rowPadding",
            "line.top >= title5.bottom + container.rowPadding",

            "more.top == line.bottom + container.buttonPadding",
            "more.height == more.intrinsicHeight",
            "more.left == container.leftPadding",
            "more.right == container.rightPadding",

            "container.height == more.bottom + container.buttonPadding"
    };


    public ConstraintParser.CassowaryVariableResolver createVariableResolver(final Solver solver, final HashMap<String, HashMap<String, Variable>> nodeHashMap) {
        ConstraintParser.CassowaryVariableResolver variableResolver = new ConstraintParser.CassowaryVariableResolver() {

            private Variable getVariableFromNode(HashMap<String, Variable> node, String variableName) {

                try {
                    if (node.containsKey(variableName)) {
                        return node.get(variableName);
                    } else {
                        Variable variable = new Variable(variableName);
                        node.put(variableName, variable);
                        if (RIGHT.equals(variableName)) {
                            solver.addConstraint(Symbolics.equals(variable, Symbolics.add(getVariableFromNode(node, LEFT), getVariableFromNode(node, WIDTH))));
                        } else if (BOTTOM.equals(variableName)) {
                            solver.addConstraint(Symbolics.equals(variable, Symbolics.add(getVariableFromNode(node, TOP), getVariableFromNode(node, HEIGHT))));
                        } else if (CENTERX.equals(variableName)) {
                           // solver.addConstraint(Symbolics.equals(variable, Symbolics.add(Symbolics.divide(getVariableFromNode(node, WIDTH), 2), getVariableFromNode(node, LEFT)));
                        } else if (CENTERY.equals(variableName)) {
                           // solver.addConstraint(Symbolics.equals(variable, Symbolics.add(new Expression(Symbolics.divide(getVariableFromNode(node, HEIGHT), 2)), getVariableFromNode(node, TOP));
                        }
                        return variable;
                    }
                } catch(DuplicateConstraintException e) {
                    e.printStackTrace();
                } catch (UnsatisfiableConstraintException e) {
                    e.printStackTrace();
                }
                
                return null;

            }

            private HashMap<String, Variable> getNode(String nodeName) {
                HashMap<String, Variable> node;
                if (nodeHashMap.containsKey(nodeName)) {
                    node = nodeHashMap.get(nodeName);
                } else {
                    node = new HashMap<String, Variable>();
                    nodeHashMap.put(nodeName, node);
                }
                return node;
            }

            @Override
            public Variable resolveVariable(String variableName) {

                String[] stringArray = variableName.split("\\.");
                if (stringArray.length == 2) {
                    String nodeName = stringArray[0];
                    String propertyName = stringArray[1];

                    HashMap<String, Variable> node = getNode(nodeName);

                    return getVariableFromNode(node, propertyName);

                } else {
                    throw new RuntimeException("can't resolve variable");
                }
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
        return variableResolver;
    }

    @Test
    public void testSimple() {
        int a = 20;
        assertEquals(20, a);
    }

    @Test
    public void testGridLayout() throws DuplicateConstraintException, UnsatisfiableConstraintException, NonlinearExpressionException {

        final Solver solver = new Solver();
        final HashMap<String, HashMap<String, Variable>> nodeHashMap = new HashMap<>();

        ConstraintParser.CassowaryVariableResolver variableResolver = createVariableResolver(solver, nodeHashMap);

        for (String constraint : CONSTRAINTS) {
            Constraint con = ConstraintParser.parseConstraint(constraint, variableResolver);
            solver.addConstraint(con);
        }

        solver.addConstraint(ConstraintParser.parseConstraint("container.width == 300", variableResolver));
        solver.addConstraint(ConstraintParser.parseConstraint("title0.intrinsicHeight == 100", variableResolver));
        solver.addConstraint(ConstraintParser.parseConstraint("title1.intrinsicHeight == 110", variableResolver));
        solver.addConstraint(ConstraintParser.parseConstraint("title2.intrinsicHeight == 120", variableResolver));
        solver.addConstraint(ConstraintParser.parseConstraint("title3.intrinsicHeight == 130", variableResolver));
        solver.addConstraint(ConstraintParser.parseConstraint("title4.intrinsicHeight == 140", variableResolver));
        solver.addConstraint(ConstraintParser.parseConstraint("title5.intrinsicHeight == 150", variableResolver));
        solver.addConstraint(ConstraintParser.parseConstraint("more.intrinsicHeight == 160", variableResolver));

        solver.updateVariables();

        assertEquals(20, nodeHashMap.get("thumb0").get("top").getValue(), EPSILON);
        assertEquals(20, nodeHashMap.get("thumb1").get("top").getValue(), EPSILON);

        assertEquals(85, nodeHashMap.get("title0").get("top").getValue(), EPSILON);
        assertEquals(85, nodeHashMap.get("title1").get("top").getValue(), EPSILON);

        assertEquals(210, nodeHashMap.get("thumb2").get("top").getValue(), EPSILON);
        assertEquals(210, nodeHashMap.get("thumb3").get("top").getValue(), EPSILON);

        assertEquals(275, nodeHashMap.get("title2").get("top").getValue(), EPSILON);
        assertEquals(275, nodeHashMap.get("title3").get("top").getValue(), EPSILON);

        assertEquals(420, nodeHashMap.get("thumb4").get("top").getValue(), EPSILON);
        assertEquals(420, nodeHashMap.get("thumb5").get("top").getValue(), EPSILON);

        assertEquals(485, nodeHashMap.get("title4").get("top").getValue(), EPSILON);
        assertEquals(485, nodeHashMap.get("title5").get("top").getValue(), EPSILON);
    }

  /*  @Test
    public void testGridLayoutUsingEditVariables() throws CassowaryError {

        final SimplexSolver solver = new SimplexSolver();
        solver.setAutosolve(true);

        final HashMap<String, HashMap<String, Variable>> nodeHashMap = new HashMap<String, HashMap<String, Variable>>();

        ConstraintParser.CassowaryVariableResolver variableResolver = createVariableResolver(solver, nodeHashMap);

        for (String constraint : CONSTRAINTS) {
            solver.addConstraint(ConstraintParser.parseConstraint(constraint, variableResolver));
        }

        Variable containerWidth = nodeHashMap.get("container").get("width");
        Variable title0IntrinsicHeight = nodeHashMap.get("title0").get("intrinsicHeight");
        Variable title1IntrinsicHeight = nodeHashMap.get("title1").get("intrinsicHeight");
        Variable title2IntrinsicHeight = nodeHashMap.get("title2").get("intrinsicHeight");
        Variable title3IntrinsicHeight = nodeHashMap.get("title3").get("intrinsicHeight");
        Variable title4IntrinsicHeight = nodeHashMap.get("title4").get("intrinsicHeight");
        Variable title5IntrinsicHeight = nodeHashMap.get("title5").get("intrinsicHeight");
        Variable moreIntrinsicHeight = nodeHashMap.get("more").get("intrinsicHeight");

        solver.addStay(containerWidth);
        solver.addStay(title0IntrinsicHeight);
        solver.addStay(title1IntrinsicHeight);
        solver.addStay(title2IntrinsicHeight);
        solver.addStay(title3IntrinsicHeight);
        solver.addStay(title4IntrinsicHeight);
        solver.addStay(title5IntrinsicHeight);
        solver.addStay(moreIntrinsicHeight);

        solver.addEditVar(containerWidth);
        solver.addEditVar(title0IntrinsicHeight);
        solver.addEditVar(title1IntrinsicHeight);
        solver.addEditVar(title2IntrinsicHeight);
        solver.addEditVar(title3IntrinsicHeight);
        solver.addEditVar(title4IntrinsicHeight);
        solver.addEditVar(title5IntrinsicHeight);
        solver.addEditVar(moreIntrinsicHeight);
        solver.beginEdit();

        solver.suggestValue(containerWidth, 300);
        solver.suggestValue(title0IntrinsicHeight, 100);
        solver.suggestValue(title1IntrinsicHeight, 110);
        solver.suggestValue(title2IntrinsicHeight, 120);
        solver.suggestValue(title3IntrinsicHeight, 130);
        solver.suggestValue(title4IntrinsicHeight, 140);
        solver.suggestValue(title5IntrinsicHeight, 150);
        solver.suggestValue(moreIntrinsicHeight, 160);

        solver.resolve();

        solver.solve();

        assertEquals(20, nodeHashMap.get("thumb0").get("top").value(), EPSILON);
        assertEquals(20, nodeHashMap.get("thumb1").get("top").value(), EPSILON);

        assertEquals(85, nodeHashMap.get("title0").get("top").value(), EPSILON);
        assertEquals(85, nodeHashMap.get("title1").get("top").value(), EPSILON);

        assertEquals(210, nodeHashMap.get("thumb2").get("top").value(), EPSILON);
        assertEquals(210, nodeHashMap.get("thumb3").get("top").value(), EPSILON);

        assertEquals(275, nodeHashMap.get("title2").get("top").value(), EPSILON);
        assertEquals(275, nodeHashMap.get("title3").get("top").value(), EPSILON);

        assertEquals(420, nodeHashMap.get("thumb4").get("top").value(), EPSILON);
        assertEquals(420, nodeHashMap.get("thumb5").get("top").value(), EPSILON);

        assertEquals(485, nodeHashMap.get("title4").get("top").value(), EPSILON);
        assertEquals(485, nodeHashMap.get("title5").get("top").value(), EPSILON);

    }
*/
  
    @Test
    public void testGridX1000() throws DuplicateConstraintException, UnsatisfiableConstraintException, NonlinearExpressionException {

        long nanoTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            testGridLayout();
        }
        System.out.println("testGridX1000 took " + (System.nanoTime() - nanoTime) / 1000000);
    }

    /*
    @Test
    public void testGridWithEditsX1000() throws CassowaryError {

        long nanoTime = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            testGridLayoutUsingEditVariables();
        }
        System.out.println("testGridWithEditsX1000 took " + (System.nanoTime() - nanoTime) / 1000000 + " ms");

    }*/

    private static void printNodes(HashMap<String, HashMap<String, Variable>> variableHashMap) {
        Iterator<Map.Entry<String, HashMap<String, Variable>>> it = variableHashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, HashMap<String, Variable>> pairs = it.next();
            System.out.println("node " + pairs.getKey());
            printVariables(pairs.getValue());
        }
    }

    private static void printVariables(HashMap<String, Variable> variableHashMap) {
        Iterator<Map.Entry<String, Variable>> it = variableHashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Variable> pairs = it.next();
            System.out.println(" " + pairs.getKey() + " = " + pairs.getValue().getValue() + " (address:" + pairs.getValue().hashCode() + ")");
        }
    }

}
