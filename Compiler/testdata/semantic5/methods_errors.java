class Test {

    public void m1() {}

    /* method with void param*/
    public void m1(void x) {}

    /* methods with some parameters */
    public void m21(boolean x) {}
    public void m22(int x) {}
    public void m23(Test x) {}

    /* some arrays */
    public void m31(boolean[] x) {}
    public void m32(int[] x) {}
    public void m33(Test[] x) {}

    /* some methods with return types */
    public boolean m41() {return true;}
    public int m42() {return 42;}
    public Test m43() {return this;}

    /* wrong return types */
    public boolean m51() {return 1;}
    public boolean m511() {return this;}
    public boolean m5111() {return new boolean[5];}

    public int m52() {return true;}
    public int m522() {return this;}
    public int m5222() {return new int[5];}

    public Test m53() {return true;}
    public Test m533() {return 5;}
    public Test m5333() {return new Test[5];}

    public boolean[] m54() {return true;}
    public int[] m544() {return 1;}
    public Test[] m5444() {return this;}

    public void test()
    {
         /* calling methods with wrong paramters */
        boolean b; int x; Test t;

        m21(x);
        m22(b);
        m23(x);
        m22(t);

        m21(new boolean[1]);
        m22(new int[2]);
        m23(new Test[2]);

        m21(b);
        m22(x);
        m23(t);

        b = m42();
        x = m41();
        b = m43();
        t = m42();
    }

    public static void main(String[] args) {
        Test test = new Test();
        test.test();
    }
}