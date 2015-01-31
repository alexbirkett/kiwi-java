package no.birkett.kiwi;

/**
 * Created by alex on 30/01/15.
 */
public class Util {
    private static double EPS = 1.0e-8;

    public static boolean nearZero(double value ) {
        return value < 0.0 ? -value < EPS : value < EPS;
    }
}
