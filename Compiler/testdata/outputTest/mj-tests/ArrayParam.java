class A {
  public boolean y;
  public int x;
  public int[] z;

  public void arr2(int[] arr) {
    arr[0] = 3;
  }

  public static void main(String[] args) {
    A a = new A();
    a.z = new int[1];
    a.arr2(a.z);
    System.out.println(a.z[0]);
  }
}
