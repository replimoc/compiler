class main
{
    public static void main(String[] a) {}
}

/* test 1 */
class one {
    public one succ;
    public void succ(one one) {
        succ(one);
        succ = one.succ.succ;
    }
}

/* test 2 */
class System {
    public System System;
    public System out;
    public void println() {
        System.out.println();
    }
}

/* test 3 */
class a {
    public b b;
}
class b {
    public a a;
    public b b() {
        b b = b();
        b.a.b.b().b().a.b.b();
        b();
        return a.b.a.b;
    }
}

/* test 4 */
class a2 {
    public int chaos() {
        boolean b= - -1==0==5<4;
        int x=5*4+new int[7+1][1];
        b=true != false;
        return chaos();
    }
}

/* test 5 */
class a3 {
    public int deepIf() {
        if (true) {
            if (true) {
                if (true) return 0;
                return 1;
            } else {
                int x= 1;
                x=2;
                return 2;
            }
        } else return 5;
    }
}