class Node {
	public int number;

	public Node parent;
	public Node next;

	public void init(int num) {
		number = num;
		parent = null;
		next = null;
	}

	public void insert(Node node) {
		Node prevNextNode = next;
		next = node;
		node.parent = this;
		node.next = prevNextNode;
	}
}

class App {
	public Node createList(int size) {
		Node firstNode = new Node();
		firstNode.init(0);

		int i = 1;
		Node prevNode = firstNode;
		while (i < size) {
			Node newNode = new Node();
			newNode.init(i);
			prevNode.insert(newNode);
			prevNode = newNode;
			i = i + 1;
		}
		
		return firstNode;
	}

	public void printNodeNums(Node firstNode) {
		Node currNode = firstNode;
		while (currNode != null) {
			System.out.println(currNode.number);
			currNode = currNode.next;
		}
	}

	public static void main(String[] args) {
		App app = new App();
		Node firstNode = app.createList(42);
		app.printNodeNums(firstNode);
	}
}
