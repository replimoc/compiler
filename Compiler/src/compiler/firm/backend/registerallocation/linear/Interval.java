package compiler.firm.backend.registerallocation.linear;

public class Interval {
	private int start;
	private int end;

	public Interval(int startEnd) {
		this.start = startEnd;
		this.end = startEnd;
	}

	public boolean contains(int line) {
		return start <= line && line <= end;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void expandEnd(int line) {
		this.end = line;
	}

	@Override
	public String toString() {
		return "[" + start + "," + end + "]";
	}
}
