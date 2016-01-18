package no.birkett.kiwi;

/**
 * Created by alex on 30/01/15.
 */
public class Symbol {

    enum Type {
        INVALID,
        EXTERNAL,
        SLACK,
        ERROR,
        DUMMY
    }

    private Type type;
    private long id;
    private String variableName;

    public Symbol() {
        this(Type.INVALID, 0);
    }

    public Symbol(Type type, long id) {
        this.type = type;
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    boolean lessThan(Symbol other) {
        return this.id < other.getId();
    }

    boolean equals(Symbol other) {
        return this.id == other.getId();
    }

}
