class A {
    public int m1()
    {
        return 1 * 2 + 4 / 5 - 3%6 * 2 * 2 * 2 * 2 * 2;
    }

    public int m2()
    {
        int a; int b;
        a = -a;
        return  a * b + 12 - a;
    }

    public int m3()
    {
        int a;
        return -a;
    }

    public int m4(){ return -1;}
}

class B {

    public boolean m1()
    {
        return 5 < 123;
    }

    public boolean m2()
    {
        int a; int b; boolean c;
        return 5 < 123 && a >= b && a - b <= b - a || c;
    }
    public boolean m3()
    {
        return -5 < 1 || !true ;
    }
}

class Test {

    public int getInt() {
        return 42;
    }

    public static void main(String[] args) {
        int a;
        int b;
        Test t;

        /* expressions in array declarations and array access */

        int[] x = new int[a * b - a / b + a % b + 123 * 12 / 10];
        x[a] = 1;
        x[5] = 1;
        x[a * b - a / b + t.getInt() * t.getInt() - 42] = 1;

        int[][] xx;

        xx[1][1] = 1;
        xx[a][b] = 1;
        xx[-a][-b] = 1;
        xx[a * t.getInt() - b * t.getInt() % 123][-b + 111] = 1;

        xx[1] = new int[t.getInt() - 12 * a + 14 / b - 123];

        int[][][] xxx;
        xxx[1][1][1] = 1;
        xxx[a][b][-a * -b] = 1;
        xxx[a * b - a / b][b * t.getInt()][-a * -b] = 1;

        xxx[1][2] = new int[a * b - t.getInt()];
        xxx[1] = new int[a * b - t.getInt()][];
    }
}