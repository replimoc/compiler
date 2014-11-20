class Main
{
    public static void main(String[] vargs) {
        System.out.println(42); /* ok */
    }
}

class A
{
	public A out;
	
    public void println(boolean b)
    {

    }

    public void foo()
    {
        System.out.println(1); /* ok */
    }

    public void bar()
    {
    	A System;
    	
        System.out.println(true); /* ok */
    }
}

class B
{
    public A System;
    
    public void foo()
    {
        System.out.println(true); /* ok */
    }

}