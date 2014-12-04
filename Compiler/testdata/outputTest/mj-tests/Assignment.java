class Assignment {
  public int a1;
  public boolean a2;
  public Assignment a3;
  public int[] a4;

  public static void main(String[] args) {
    Assignment a = new Assignment();
    Assignment x = a.a3 = a;
    a.a4 = new int[4];
    System.out.println(x.a3.a1);
    System.out.println(x.f1());
    x.f2();
    x.f3();
    System.out.println(a.a4[0]*(a.a4[0] = 3));
  }

  public int f1() {
    return this.a1 = 5;
  }

  public boolean f2() {
    return this.a2 = true;
  }

  public Assignment f3() {
    return this.a3 = new Assignment();
  }

}
