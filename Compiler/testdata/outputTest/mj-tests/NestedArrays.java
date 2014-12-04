class A {
  public boolean y;
  public int x;
  public int[] z;

  public void nestedArray() {
    int[][] a = new int[2][];
    a[0] = new int[3];
    int[] b = a[0];
  }
}

class Main {
  public static void main(String[] args) {
    A a = new A();
    a.nestedArray();
    System.out.println(42);
  }
}
