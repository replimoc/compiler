class Test {

    /* void methods */

    void m10() {};

    void m11() { return };

    /* return new primitive types and variables */

    int m20() {return 1;}
    int m21() {int x = 5 ; return x;}
    boolean m22() {boolean x = true ; return x;}
    boolean m23() {int x = 5 ; return x == 5;}

    /* methods with return types must have returns in every path */

    int m31() {
        if (true) {
            return 1;
        } else {
            return 2;
        }
    };

    int m32() {
        int x;
        if (true) {
            x = 1;
        } else {
            x = 2;
        }
        return x;
    };

    int m33() {
        int x;
        if (true) {
            return 5;
        } else {
            x = 2;
        }
        return x;
    };

    int m34() {
        while(true);
        return 1;
    }

    int m35() {
        while(true) {
            return 1;
        }
    }

    /* return objects */

    Test m40 () { return null; }
    Test m41 () { return this; }
    Test m42 () { return new Test(); }
    Test m43 () { Test x = new Test(); return x; }

    /* arrays */

    boolean[] m50 () {return new boolean[1];}
    int[] m51 () {return new int[1];}
    Test[] m52 () {return new Test[1];}

    /* methods with parameters */
    void f1(int x) {};
    void f2(int x, int y) {};
    void f3(int x, boolean b) {};
    void f4(Test x) {};
    void f5(Test[] x) {};
    void f6(int[] x) {};
    void f7(boolean[] x) {};

    public static void main(String[] args) {

        /* test correct parameters */

        int x; boolean b; Test test;
        int[] xx; boolean bb[]; Test[] tt;

        f1(0); f1(x); f1(x+1); f1(x-1); f1(xx[1]);
        f2(1,2); f2(x,x); f2(x*x, x/x);
        f3(1,true); f3(x%1,b); f3(1, bb[1]);
        f4(null); f4(new Test()); f4(test); f4(tt[1]);

        f5(new Test[1]); f5(tt);
        f6(new int[1]); f6(xx);
        f7(new boolean[1]); f7(bb);

        /* test correct return type assignment */

        int i2 = m20();
        x = m21();
        boolean b2 = m22();
        b = m23();
        Test test1 = m41();
        test = m40();

        xx[0] = m20();
        bb[0] = m22();
        tt[0] = m43();

        boolean bbb[] = m50();
        bb = m50();
        int yyy = m51();
        xx = m51();
        Test ttt[] = m52();
        tt = m52();
    }
}