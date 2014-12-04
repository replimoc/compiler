class LocalVarDeclScope {
  public static void main(String[] args) {
    int i = (i = 42);
    System.out.println(i);
  }
}
