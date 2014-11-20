class Test {

    public static void main(String[] args) {

        boolean b1;
        boolean b2 = true;
        boolean b3 = false;
        boolean[] bb = new boolean[5];

        int x; int y;
        Test t1; Test t2;

        /* int expressions */

        if (x) { } ;
        while (y) { } ;

        /* expressions +,-,*,/,%,- */

        if (x + y) { } ;
        if (x * y) { } ;
        if (-x) { } ;
        while (x + y) { } ;
        while (x * y) { } ;
        while (-x) { } ;

        /* assignment expression */

        if ((x = 1)) {};
        while ( (x = 0)) {};

    }

}