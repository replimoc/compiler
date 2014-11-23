class A {
    public B b;

    public void a() {
        a();
    }

    public void b() {
        b.a.a();
    }

    public B c()
    {
        return b.a.c();
    }
}

class B
{
    public A a;
    public B b;

    public void b()
    {
        b();
    }

    public void a()
    {
        a.b();
    }

    public void rec()
    {
        this.a.b.rec();
    }

    public void rec2(B b)
    {
        this.b.a.c().rec2(b);
    }

}

class Recursion
{
    public static void main(String[] args) {
        A a = new A();
        B b = new B();

        b = a.c();
        b.rec2(b);
    }



}