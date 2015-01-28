class Div {

    public int div(int x, int y) {
        return x / y;
    }

    public void dotest() {
        int a1 = 8545;
        int a2 = 879;
        int a3 = -856;
        int a4 = -1500;

        int i = 879;

        while (i <= (879 * 500 + 849)) {

            { /* a1*/
                int res11 = i / a1;
                int exp11 = div(i, a1);

                if (exp11 != res11) {
                    System.out.println(exp11);
                    System.out.println(res11);
                }
            }

            { /* -a1*/
                int j = -1 * i;
                int res12 = j / a1;
                int exp12 = div(j, a1);

                if (exp12 != res12) {
                    System.out.println(exp12);
                    System.out.println(res12);
                }
            }

            { /* a2*/
                int res21 = i / a2;
                int exp21 = div(i, a2);

                if (exp21 != res21) {
                    System.out.println(exp21);
                    System.out.println(res21);
                }
            }

            { /* -a2*/
                int j = -1 * i;
                int res22 = j / a2;
                int exp22 = div(j, a2);

                if (exp22 != res22) {
                    System.out.println(exp22);
                    System.out.println(res22);
                }
            }

            { /* a3 */
                int res31 = i / a3;
                int exp31 = div(i, a3);

                if (exp31 != res31) {
                    System.out.println(exp31);
                    System.out.println(res31);
                }
            }

            {/*  -a3 */
                int j = -1 * i;
                int res32 = j / a3;
                int exp32 = div(j, a3);

                if (exp32 != res32) {
                    System.out.println(exp32);
                    System.out.println(res32);
                }
            }

            { /* a4 */
                int res41 = i / a4;
                int exp41 = div(i, a4);

                if (exp41 != res41) {
                    System.out.println(exp41);
                    System.out.println(res41);
                }
            }

            {  /* -a4 */
                int j = -1 * i;
                int res42 = j / a4;
                int exp42 = div(j, a4);

                if (exp42 != res42) {
                    System.out.println(exp42);
                    System.out.println(res42);
                }
            }

            i = i + 1;
        }
    }


    public static void main(String[] args) {
        Div div = new Div();
        div.dotest();

        System.out.println(42);
    }

}