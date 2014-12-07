class A {
  public int[] z;

  public int[] arrayReturn() {
    return new int[5];
  }
}

class Main {
  public static void main(String[] args) {
    A a = new A();
    a.z = a.arrayReturn();
  }
}
