package dev.jorel.commandapi.wrappers;

/**
 * A class representing a range of integers
 */
public class IntegerRange {

	private final int low;
	private final int high;
	
	/**
	 * Constructs an IntegerRange with a lower bound and an upper bound
	 * @param low the lower bound of this range
	 * @param high the upper bound of this range
	 */
	public IntegerRange(int low, int high) {
		this.low = low;
		this.high = high;
	}
	
	/**
	 * The lower bound of this range.
	 * @return the lower bound of this range
	 */
	public int getLowerBound() {
		return this.low;
	}
	
	/**
	 * The upper bound of this range.
	 * @return the upper bound of this range
	 */
	public int getUpperBound() {
		return this.high;
	}
	
	/**
	 * Determines if an int is within range of the lower bound (inclusive) and the upper bound (inclusive). 
	 * @param i the int to check within range
	 * @return true if the given int is within the declared range
	 */
	public boolean isInRange(int i) {
		return i >= low && i <= high;
	}
	
}
