/* no semantic error*/
class A {
    public static void main(String[] args) {
    }

    public int missingReturn(boolean a, boolean b, boolean c, boolean d) {
        if (a) {
            if (b) {
                if (c) {
                    if (d) {
                        return 1;
                    }
                } else {
                    return 1;
                }
            }
            return 1;
        } else if (b) {
            return 1;
        } else {
            while (a && b) {
                return 1;
            }
            return 1;
        }
    }
}