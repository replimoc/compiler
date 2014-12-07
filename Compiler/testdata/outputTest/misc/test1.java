class _
{
  public static void main(String[] args) {
    int[][] data = new int[42][];
    int[] i = data[0];
    int[] a;
    /* as a and i should be null, they are equal */
    if (a == i) {
      System.out.println(42);
    }
    
    a = new int[0];
    if (a == i) {
      System.out.println(42);
    }
    
    _ _ = new _();
    _.i = 0;
    _.test();
  }
  
  public int i;
  
  public void test() {
    if (i > 10) {
      return;
    }
    
    i = i + 1;
    System.out.println(i);
    
    _ _ = new _();
    _._();
    test();
  }
  
  public void _() {
  }
}