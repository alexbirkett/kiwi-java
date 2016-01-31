package no.birkett.kiwi;

/**
 * Created by alex on 30/01/15.
 */
public class Variable {

    private String name;

    private double value;

    public Variable(String name) {
        this.name = name;
    }

    public Variable(double value) {
    }
    
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "name: " + name + " value: " + value;
    }

}
