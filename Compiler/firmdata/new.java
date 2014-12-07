class M {
    public static void main(String[] args) {
        B b;
        A a = new A();
        int[] x = new int[11];
        B[] bb = new B[10+1];

        b = new B();
    }
}

class A {
    public int x;
    public int y;
    public B bb;

    public void f() {
        return;
    }

    public int m(B b) {
        this.x = b.x + b.y;
        this.y = b.x - b.y;
        return b.y;
    }
}

class B {
    public int x;
    public int y;
    public A aa;

    public int m(A a) {
        this.x = a.x + a.y;
        this.y = a.x - a.y;
        return a.x;
    }
}
