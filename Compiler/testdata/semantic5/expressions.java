class A {}
class B {}

class Test {

    public static void main(String[] args) {

        int x;
        int y = 5;
        boolean b1;
        boolean b2;
        Test t;

        /* type of system.out.println arg - is int */

        System.out.println(x);
        System.out.println(y);

        /* unary expressions */

        y = -x; x = -1;
        b1 = !true; b2 = !b1;

        /* *,/,% */

        x = 5 * 15352;
        y = x * x;
        y = 2345 / y;
        y = x % (-y);
        x = -5 / -6;

        /* +,- */

        y = x + x; y = x - x;
        y = (x * x) + (y / y) - (x % y);

        /* <. <=, >, >= */

        b1 = x < y; b1 = y <= x; b1 = x > x; b2 = x >= 5;

        b1 = x*x - y % y <= 522;
        b1 = x*x -y+y >= 5 * 6 - 7 + 123 + x;

        /* ==, != */

        b1 = b1 == b2;
        b1 = x == y;
        b2 = b1 != b2;
        b2 = x != y;
        b1 = 25 == 52;
        b1 = 25 != 52;

        b2 = t == null; b2 = t != null;
        b2 = null == null; b2 = null != null;

        /* &&, || */

        b1 = true && false;
        b1 = true || false;
        b1 = b1 && b2;
        b1 = !b1 && !b2;
        b1 = b1 || b2;
        b1 = !b1 || !b2;
        b1 = (x < y) || (x > y) && (x * x <= x / x) || (((((false)))));

        /* assignment */
        A a = new A();
        A[] aa = new A[1];
        aa[0] = new A();
        A a = null;
        aa[0] = null;
    }
}