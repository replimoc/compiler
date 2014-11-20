class Main
{
    public void m(){ this.m();}

    /* correct - args can be called whatever */
    public static void main(String[] vargs) {

    }
}

class MultiScope {
	
    public void correctMultiScope() {
        int x;
        {
            int x;
            {
                x = 1;
                boolean x;
                x = true;
                x = false;
                {
                    int x;
                }
            }
            x = 5; x = 7;
        }
        x = x * x - x / x + x;
    }

}

class MethodsFields
{
    public int m;
    public void m() {} /* this is not an error */

    public void m2() { boolean m; } /* this is also not an error */
}