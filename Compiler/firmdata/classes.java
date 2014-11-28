class M {
    public static void main(String[] args) {
        A a = new A();
        B b = new B();
        a.m(b);
        b.m(a);
    }
}

class A {
    public int x;
    public int y;
    public B bb;

    public void f()
    {
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
