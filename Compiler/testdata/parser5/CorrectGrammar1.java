/**
 * This test is for basic program structure: classes, methods, variables, etc...
 */

class Cl1 {

    public CLAZZ clazzy;
    /** yeah, that is a varible **/
    public void main;
    public int[] list;
    public int[][] matrix;
    public int[][][] cube;
    public Cl2 cl2;

    /**
     * do we have constructors in mini-java?
     * @return new this! (or something else...)
     */
    public Cl1 Cl1()
    {
        this.clazzy = null;
        Arrays arrays = new Arrays();
        this.list = arrays.getList(100);
        this.matrix = arrays.getMatrix(100);
        this.cube = arrays.getCube(100);

        this.wtf = this.Cl1();
    }
}

class Arrays {

    public int getInt()
    {
        int x;
        int y = 5;
        int z = x;
        Cl1 cl1 = null;
        int a = a + b;

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
        int[] matrix = new int[10][];
        return matrix;
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
        Cl1 cl2 = Parameters.i_accept_no_params(cl1, null);
        retur false;
    }

    public void i_accept_one_param(Cl1 cl1)
    {
        ;;;;;;;;;;;;;;;;;;;;;;;;;;;
    }
    public void i_accept_a_lot_of_params(int a, int[] vec, int[][] A, int[][][] cube) {}

    public void i_accept_no_params(void a,void b,void c) {
        a + b - c + (d) - (-x);
        return;
    }

    public void i_accept_bools (boolean one, boolean two, boolean[] three_four_five)
    {
        boolean a = one || two;
        boolean b = three_four_five[0] && three_four_five[1];
        boolean c = !three_four_five[3];

        boolean d = (a && b) || (c && !b) || (~(a || c));

        boolean b_true = true;
        boolean b_false = false;
    }
}

class MainClass {

    /**
     * This is not a main, really not MAIN :)
     * @param cmlds
     */
    public static void not_a_main ( String[] forbidden_fruit)
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

        return 0;
        return 1;
        return 2+4+129;
    }
}