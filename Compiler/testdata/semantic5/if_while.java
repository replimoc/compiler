class Test {

    public static void main(String[] args) {

        boolean b1;
        boolean b2 = true;
        boolean b3 = false;
        boolean[] bb = new boolean[5];
        int x; int y;

        /* booleans/true/false */

        if (b1) { } ; if (true) { } ; if (false) { } ; if (bb[1]) { };
        while (b1) { } ; while (true) { } ; while (false) { } ; while (bb[1]) { } ;

        /* expressions: unary !, relations, equality, logical or/and */

        if (!b1) { } ; if (!true) { } ; if (!false) { } ; if (!bb[1]) { };
        while (!b1) { } ; while (!true) { } ; while (!false) { } ; while (!bb[1]) { } ;

        if (x < y) { } ; if (x <= y) { } ; if (x > y) { } ; if (x >= y) { };
        while (x < y) { } ; while (x <= y) { } ; while (x > y) { } ; while (x >= y) { };

        if (b1 == b2) { } ; if (b1 != b2) { } ;
        while (b1 == b2) { } ; while (b1 != b2) { } ;

        if (b1 && b2) { } ; if (b1 || b2) { } ; if (b1 && false) { } ; if (bb[1] || true) { };
        while (b1 && b2) { } ; while (b1 || b2) { } ; while (b1 && false) { } ; while (bb[1] || true) { };

        /* assignment expression */

        if ((b1 = true)) {};
        while ( (b1 = true)) {};

        if ((x = 5) != 5) {};
        while ( (y = 0) == 0) {};

    }

}