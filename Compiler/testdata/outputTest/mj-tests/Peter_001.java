class Test 
{
    
    public int foo;
    
    public static void main(String[] args) 
    {
        Test t = new Test();
        t.foo = 42 * 1337;
        t.foo();
    }

    public void foo() {
        System.out.println(foo);
        
        {
            int foo = 42;
            System.out.println(foo);
        }
        {
            int foo = 1337;
            System.out.println(foo);
        }
    }
}
