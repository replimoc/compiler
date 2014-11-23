class foo {
    public static void main(String[] args){
        foo foo = new foo();
        foo.foo = 5;
        foo.foo(foo);
    }

    public int foo;
    public void foo(foo foo) {
        foo.foo = this.foo;
        foo = new foo();
        {
            foo foo = new foo(); /* this is an error in java 8 */
            foo.foo = this.foo;
        }

        int bar = 0;
        while(foo.foo > 0)
        {
            bar = bar + foo.foo;
            foo.foo = this.foo - 1;
        }
        return ;
    }
}

class bar
{
    public bar bar;
}

class foo2
{
    public foo2[][] foo2;
}