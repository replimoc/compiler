class C
{
    public void mb(boolean[] b){}
    public void mi(int[] b){}
    public void mc(C[] b){}

    /*no null in return types*/

    public boolean[] mb2(){ return null;}
    public int[] mi2() {return null;}
    public C[] mc2(){return null;}


    public static void main(String[] args) {

        int x; int y;

        /*no null in statements*/
        boolean[] b = null;
        int[] x = null;
        C[] cc = null;

         /*no null in parameters*/
        C c = new C();
        c.mb(null);
        c.mi(null);
        c.mc(null);
    }
}