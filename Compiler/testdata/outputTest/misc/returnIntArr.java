class Main {
  public int[] arrayReturn() {
    return new int[5];
  }

  public static void main(String[] args) {
    Main a = new Main();
    int[] b = a.arrayReturn();
  }
}