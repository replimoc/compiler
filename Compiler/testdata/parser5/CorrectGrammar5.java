/**
 * test something else, including syntactically correct but semantically incorrect constructs;
 */

class Oo {

    public static void main ( String[] args)
    {
        (null) = false;
        (((((null))))) = true;

        void[][][][][][][][][][][][][][][][] x;

        /* the first one is semantically correct */
        identifier.m1().m2(42).m3(42,24).identifier[].identifier[][][];
        true.m1().m2(42).m3(42,24).identifier[].identifier[][][];
        5.m1().m2(42).m3(42,24).identifier[].identifier[][][];

        new identifier().ident[].ident;

        (null) || (!null) = (5 + 5) = (5*5) = 123;
        int a = b = c = d = e = f = g;

        x[null][true][!false] = m1(expr1, expr2, (a = b) == 0 || 1, new Ident());

        int minus_zero = -0;

        /* at this point my fantasy has expired (please don't copy this comment */
    }
}