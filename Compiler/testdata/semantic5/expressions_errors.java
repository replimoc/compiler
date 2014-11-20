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

        System.out.println(false);
        System.out.println(new Test());

        /* unary expressions */

        b1 = !y;
        b2 = -y;

        /* *,/,% */

        y = b1 * b2;
        y = b1 / b2;
        y = b1 % b2;
        y = !b1 * !b2;
        y = -x * !b2;

        /* +,- */

        y = b1 + b2;
        y = b1 - b2;
        y = x * y + b1;
        y = x * y - b1;

        /* <. <=, >, >= */

        x = x < x;
        x = x <= y;
        x = x > x;
        x = x >= x;

        x = 5 < 5;
        
        b1 = true < 5;
        b2 = 5 <= false;
        b1 = 5*5 -5 + 123 > null;
        b2 = null >= 5;

        /* ==, != */

        b1 = true == 5;
        b1 = 5 != true;

        b1 = t == 0;
        b1 = t != 0;
        b2 = t == true;
        b2 = t != true;

        /* &&, || */

        b1 = 1 && x;
        b1 = y || b2;

        /* array length */
        boolean[] bb = new boolean[123];
        bb.length;

        /* assignment */
        A a = new B();
        A[] aa = new B[1];
        A[] aa = new A[1]; /*no error*/
        aa[1] = new B();

    }
}