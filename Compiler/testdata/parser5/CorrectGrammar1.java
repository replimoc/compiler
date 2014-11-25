/**
 * This test is for basic program structure: classes, methods, variables, etc...
 */

class Cl1 {

    public CLAZZ clazzy; /* semantic error */
    public void main; /*semantic error*/
    public int[] list;
    public int[][] matrix;
    public int[][][] cube;
    public Cl2 cl2; /* semantic error */

    /**
     * do we have constructors in mini-java?
     * @return new this! (or something else...)
     */
    public Cl1 Cl1()
    {
        this.clazzy = null;
        Arrays arrays = new Arrays();
        this.list = arrays.getList(100);
        this.matrix = arrays.getMatrix(100); /*semantic error - function accepts no param*/
        this.cube = arrays.getCube();

        this.wtf = this.Cl1();  /*semantic error*/
        return null;
    }
}

class Arrays {

    public int getInt()
    {
        int x;
        int y = 5;
        int z = x;
        Cl1 cl1 = null;
        int a = a + x;  /* no semantic error*/
        int c = x + b;  /*semantic error*/

        return x + y * z;
    }

    public int[] getList(int len)
    {
        int[] arr = new int[len];
        int first = arr[0]; int last = arr[1];
        return arr;
    }

    public int[][] getMatrix()
    {
        int[] matrix = new int[10][];  /*semantic error - wrong type*/
        return matrix;  /*semantic error - wrong return type */
    }

    public int[][][] getCube()
    {
        int[][][] cube = new int[10][][];
        return cube;
    }
}

class Parameters {

    public void i_accept_no_params()
    {
        Cl1 cl1 = new Cl1();
        Cl1 cl2 = i_accept_no_params(cl1, null);  /*semantic error - wrong params, wrong return type*/
        return false;  /*semantic error*/
    }

    public void i_accept_one_param(Cl1 cl1)
    {
        ;;;;;;;;;;;;;;;;;;;;;;;;;;;
    }
    public void i_accept_a_lot_of_params(int a, int[] vec, int[][] A, int[][][] cube) {}

    public void i_accept_no_params2(void a,void b,void c) { /* semantic error x3 */
        a + b - c + (d) - (-1);  /*semantic error void to + and void to - and d undefined and no expression statement*/
        return;
    }

    public void i_accept_bools (boolean one, boolean two, boolean[] three_four_five)
    {
        boolean a = one || two;
        boolean b = three_four_five[0] && three_four_five[1];
        boolean c = !three_four_five[3];

        boolean d = (a && b) || (c && !b) || ((a || c));

        boolean b_true = true;
        boolean b_false = false;
    }
}

class MainClass {

    /**
     * This is not a main, really not MAIN :)
     * @param cmlds
     */
    public static void not_a_main ( String[] forbidden_fruit)  /*semantic error must be called main, all other semantic errors in this method ignored */
    {
        /* hello world! (in ascii) */
        System.out.println(104);
        System.out.println(101);
        System.out.println(108);
        System.out.println(108);
        System.out.println(111);
        System.out.println(32);
        System.out.println(119);
        System.out.println(111);
        System.out.println(114);
        System.out.println(108);
        System.out.println(100);
        System.out.println(33);

        /* errors not detected due to not_a_main is not main */
        return 0;
        return 1;
        return 2+4+129;
    }
    
    /* semenatic error - there is no main*/
}
