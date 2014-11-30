/**
 * creation and access to one-dimentional arrays
 */
class Main
{
	public static void main(String[] args) {
		int[] arr = new int[10];
		arr[0] = 1;
		int z = arr[0];

		/*int[] arr2 = arr;*/

		Element[] elements = new Element[2];
		elements[z] = new Element();
		Element e = elements[z];
	}
}

class Element {}