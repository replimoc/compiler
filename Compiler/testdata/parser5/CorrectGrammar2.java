/**
 * This test is for expressions
 */

class Expressions {

    public static void main ( String[] args)
    {
        int a; int b; int c; int d;

        /* unary expression */
        a = a; a = !a; a = -a;

        /* MultiplicativeExpression */
        a = a * b; a = a / c; a = a % d;

        /* AdditiveExpression */
        int x = a + (a * b);  x = b - c * d;

        /* RelationalExpression */
        boolean b1 = a + b = c + d * d;
        boolean b2 = a + b <= c + d * d;
        boolean b3 = a + b % d > c + d * d;
        boolean b4 = a + b >= c + d * d;

        /* EqualityExpression */
        a != b; a != (b < c); a * b * c - d * d < 125 == true;

        /* LogicalAnd */
        c && d; a+b && c + d; a * b % d && c + d;

        /* LogicalOr */
        a ||b; a || c && d; a+b || c + d; a * b % d || c + d && c - d;
    }
}