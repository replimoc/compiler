/**
 * comma after last parameter
 */

class Error {

    public void method()
    {
        x[null][true][!false] = (m1(expr1, expr2, (a = b) == 0, new Ident(), /* error */ ));
    }
}