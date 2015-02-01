# kiwi-java
A Java port of the [Kiwi](https://github.com/nucleic/kiwi), a C++ implementation of the Cassowary constraint solving algorithm

## Background
This project was created by porting [Kiwi](https://github.com/nucleic/kiwi) line for line to Java. The objective is to create a faster Java implementation of the Cassowary constraint solving algorithm.

## Example usage

        Solver solver = new Solver();
        Variable x = new Variable("x");
        Variable y = new Variable("y");

        // x = 20
        solver.addConstraint(Symbolics.equals(x, 20));

        // x + 2 == y + 10
        solver.addConstraint(Symbolics.equals(Symbolics.add(x,2), Symbolics.add(y, 10)));

        solver.updateVariables();
        
        System.out.println("x " + x.getValue() + " y " + y.getValue());
        // x == 20
        // y == 12


