
public enum Method {

	QLEARNING("Q-learning"), RANDOM("Random"), GREEDY("Greedy"), GREEDY_29("Greedy-29");
	
	private String text;
	
	private Method(String aText) {
		text = aText;
	}
	
	public String toString() {
		return text;
	}
}
