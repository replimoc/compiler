/**
 * creation and access to one-dimentional arrays
 */
class Main
{
	public static void main(String[] args) {
		Element head = new Element();
		head.value = 10;
		int z = head.value;

		Element tail = new Element();
		tail.value = 5;

		head.next = tail;

		head.printValue();
		tail.printValue();

		int v = tail.getValue();

		System.out.println(v);
	}
}

class Element {
	public int value;
	public Element next;

	public void printValue() {}

	public int getValue() {return 0;}
}