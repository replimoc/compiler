class AveryNode {
	public int f;
	public int p;
	public int rho;
	public int depth;
	public int numberOfPlayers;
	public boolean good;
	public AveryNode goodChild;
	public AveryNode badChild;

	public void initializeChildren() {
		if (depth < numberOfPlayers - 1) {
			goodChild = new AveryNode();
			badChild = new AveryNode();
		}
	}

	public int computerho(int p, int g, int b) {
		return p*g + (1 - p) * (1 - b);
	}

	public void construct1(int p_initial) {
		p = p_initial;
		f = 1;
		rho = computerho(p, g, b);
		depth = 0;
		
		initializeChildren();
	}
		
	public void construct2(AveryNode parent, boolean good) {
		depth = parent.depth + 1;
		this.good = good;
		
		if (good) {
			f = parent.rho * parent.f;
			p = (g * parent.p) / parent.rho;
		} else {
			f = 1 - parent.rho * parent.f;
			p = ((1-g) * parent.p) / (1 - parent.rho);
		}
		rho = computerho(p, g, b);
		
		initializeChildren();
	}
}

class AveryTree {
	public int b;
	public int g;
	public int numberOfPlayers;
	public AveryNode root;
	
	public AveryTree AveryTree() {
		this.b = 5;
		this.g = 2;
		this.numberOfPlayers = 4;
		root = new AveryNode();
		root.construct1(1);
	}

	public static void main(String[] args) {
		AveryTree tree = new AveryTree();
	}
}
