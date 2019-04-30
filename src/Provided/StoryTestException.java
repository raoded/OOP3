package Provided;


import java.util.List;

public abstract class StoryTestException extends Exception {
	
	/**
	 * Returns a string representing the sentence 
	 * of the first Then sentence that failed
	 */
	public abstract String getSentance();
	
	/**
	 * Returns a list of strings representing the expected values from the story
	 * of the first Then sentence that failed.
	 */
	public abstract List<String> getStoryExpected();
	
	/**
	 * Returns a list of strings representing the actual values.
	 * of the first Then sentence that failed.
	 */
	public abstract List<String> getTestResult();
	
	/**
	 * Returns an int representing the number of Then sentences that failed.
	 */
	public abstract int getNumFail();
}
