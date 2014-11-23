/* tests by fortytwo */

class Typed {

    public int integer;

    public void foo(int x) {
        bar();
    }

    public void bar() {
        this.integer = 0;
    }
}

class Two {

    public Typed x;

    public void foo() {
        x = new Typed();
        x.integer = 42;
        x.foo(21);
        {
            int x = 0;
            {
                boolean x = true;
            }
        }
        x.bar();
    }
}

class NestedNaming {

    public int foo;

    public void foo(int foo) {
        this.foo = foo;
    }

    public static void main(String[] args) {

    }
}