class Test {

    public int m() {return 1;}

    public int foo() {
        this = new Test(); /* error - can't assign to this */
    }

    public static void main(String[] args) {

        int x = 5; /* ok, assignment to lvalue */
        int y = m();
        Test t = new Test();
        boolean b = true;
        boolean[] bb = new boolean[123];
        (new boolean[123])[0] = true; /* ok ? (at least I think so ) */

        t.m() = 5; /* error - assigment to rvalue */
        x + y = 5;
        x - y = 5;
        x * y = 5;
        x / y = 5;
        x % y = 5;
        -x = 5;
        new Test() = new Test();
        new int[5] = new int[4];

    }
}