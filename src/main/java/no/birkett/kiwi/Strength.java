package no.birkett.kiwi;

/**
 * Created by alex on 30/01/15.
 */
public class Strength {

    public static final double REQUIRED = create(1000.0, 1000.0, 1000.0);

    public static final double STRONG = create(1.0, 0.0, 0.0);

    public static final double MEDIUM = create(0.0, 1.0, 0.0);

    public static final double WEAK = create(0.0, 0.0, 1.0);


    public static final double create(double a, double b, double c, double w) {
        double result = 0.0;
        result += Math.max(0.0, Math.min(1000.0, a * w)) * 1000000.0;
        result += Math.max(0.0, Math.min(1000.0, b * w)) * 1000.0;
        result += Math.max(0.0, Math.min(1000.0, c * w));
        return result;
    }

    public static final double create(double a, double b, double c) {
        return create(a, b, c, 1.0);
    }

    public static final double clip(double value) {
        return Math.max(0.0, Math.min(REQUIRED, value));
    }
}
