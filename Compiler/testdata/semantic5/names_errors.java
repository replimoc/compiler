class Test { }

class Test2 { }

/* error - class with the same name*/

class Test {

}

class Main
{
    public void m(){ this.m();}

    /* correct - args can be called whatever */
    public static void main(String[] vargs) {

        /* error - vargs cannot be used */

        vargs[5];

	/* no error, vargs without access are allowed */
        vargs;

        /* error - this inside of main */

        this.m();
    }
}

class MultiFields
{
    public int x;
    public int x; /* error - same x */

    public  boolean y;
    public int y; /*error -  same y but different type */

    public void z; /*error -  void is not allowed */
}

class MultiMethods
{
    /* error - two methods with the same signature */
    public void  m1() {}
    public void  m1() {}

    /* error - two methods with different return type */
    public int m2() { return 1;}
    public boolean m2() { return true;}

    /* error - polymorphic functions?*/
    public void m3(int x) { }
    public void m3(boolean x) { }
}

class MultiScope {

    public void incorrectMultiScope() {
        int x;
        int x; /* error -  */

        int y;
        boolean y;/* error -  */

        int z;
        {
            boolean z;
            z = 1; /* error - z is bool */
        }
        z = true; /* error - z is int */
    }
}

class MissingDeclaration
{
    public void m()
    {
        m = 5;
        m = true;
        {
            m = 4;
        }
    }
}
