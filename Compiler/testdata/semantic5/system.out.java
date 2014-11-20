class Main
{
    public static void main(String[] vargs) {
        System.out.println(true); /* error */
    }
}

class A
{
    public A System;
    public A out;

    public void println(boolean b)
    {

    }

    public void foo()
    {
        System.out.println(1); /* error */
    }

    public void bar()
    {
        System.out.println(true); /* ok */
    }
}