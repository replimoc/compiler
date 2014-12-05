class Void {

  public void noop() {
    /* this breaks the probably commonly used hack of simply prepending a Return node to End*/
    return;
  }

  public static void main(String[] args) {
    System.out.println(42);
  }
}
