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
        identifier.m1().m2(42).m3(42,24).identifier[24+44].identifier[x][y][zZ];
        true.m1().m2(42).m3(42,24).identifier[a < b + c].identifier[(((null)))][false][true || false < false];
        5.m1().m2(42).m3(42,24).identifier[a+b%c%12345].identifier[new int[x]][new integer()][1];

        new identifier().ident[c==c==c!=c].ident;

        (null) || (!null) = (5 + 5) = (5*5) = 123;
        int a = b = c = d = e = f = g;

        x[null][true][!false] = m1(expr1, expr2, (a = b) == 0 || 1, new Ident());

        int minus_zero = -0;

        new basic_type[42][][][15];

        !null * -false;

        c = a - -b;

        this[15] = 1;

        /* at this point my fantasy has expired (please don't copy this comment */
    }
}