class MultiArray {

    public int[] arr;
    public int[][] matrix;
    public int[][][] cube;

    public void testCorrect()
    {
        /* assign values */
        int max = 10;

        arr = new int[max];
        this.arr = new int[max];

        {
            this.matrix = new int[max][];
            int j = 0;
            while (j < max) {
                matrix[j] = new int[max];
                j = j + 1;
            }
        }

        /* assign in nested loops */
        this.cube = new int[max][][];
        int i = 0; int j = 0;
        while (i < max) {
            cube[i] = new int[max][];

            while (j < max) {
                cube[i][j] = new int[max];
                j = j + 1;
            }
            i = i+1;
        }

        /*access in all dimensions*/
        int x;
        x = arr[0];
        x = matrix[0][0];
        x = cube[0][0][0];

    }

    public void testError()
    {
        /* wrong types*/
        this.arr = new boolean[1];
        this.matrix = new boolean[1][];
        this.cube = new boolean[1][][];

        /* wrong dimensions */
        this.arr = new int[1][];
        this.matrix = new int[1][][];
        this.cube = new int[1];

        /*wrong types in second dimension*/
        this.matrix[0] = new boolean[1];
        this.cube[0] = new boolean[1][];
        this.cube[0][0] = new boolean[1];

        /* assignment for wrong type */
        boolean y;
        y = arr[0];
        y = matrix[0][0];
        y = cube[0][0][0];
    }

    public static void main(String[] args) {
        MultiArray a = new MultiArray();
        a.testCorrect();
    }
}