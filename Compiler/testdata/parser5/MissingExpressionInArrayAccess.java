/**
 * array access without expression
 */

class Error {
    public void method()
    {
        int[] x = new int[5];
        void y = x[5+125][/* here should be an expression */];
    }
}