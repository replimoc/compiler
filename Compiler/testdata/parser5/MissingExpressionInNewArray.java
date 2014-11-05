/**
 * array access without expression
 */

class Error {
    public void method()
    {
        int[] x = new int[null][][];
        int[] x = new int[/* here should be an expression */][][];
    }
}