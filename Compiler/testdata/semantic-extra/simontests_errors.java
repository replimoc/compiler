/* test 1 */
class a {
    public static void main(String[] a) {}
    public int main() {
        return 0;
    }/* two mains: overloading special case */
}

/* test 2 */
class b {
    public int a() {
        return 0;
    }
    public boolean a(int x) {/* overloading is forbidden */
        return true;
    }
}

/* test 3 */
class c {
    public int x;
    public void y() {
        x=null; /* null to primitive type */
    }
}

/* test 4 */
class d {
    public int x;
    public void y() {
        x[1]=x[2];/* array access to basic type */
    }
}

/* test 5 */
class e {
    public a System;
    public void b() {
        System.out.println(42);/* System defined, but not out */
    }
}