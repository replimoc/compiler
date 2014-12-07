/**
 * creation and access to one-dimentional arrays
 */
class Main
{
	public static void main(String[] args) {
		int[] arr = new int[10];
		arr[0] = 1;
		int z = arr[0];

		System.out.println(z);

		int[] arr2 = arr;

		System.out.println(arr2[0]);

		Element[] elements = new Element[2];
		elements[z] = new Element();
		Element e = elements[z];

		System.out.println(e.x);
	}
}

class Element {
	public int x;
}