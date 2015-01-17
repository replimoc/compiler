package compiler.utils;

/**
 * some useful math functions
 */
public class MathUtils {

    /**
     * Math.floor (x/y)
     * code from:
     * http://stackoverflow.com/questions/27643616/ceil-conterpart-for-math-floordiv-in-java
     */
    public static long floorDiv(long x, long y) {
        long r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

}
