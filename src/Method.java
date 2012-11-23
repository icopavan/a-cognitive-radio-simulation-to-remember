
public enum Method {

	QLEARNING("Q-learning"), RANDOM("Random");
	
	private String text;
	
	private Method(String aText) {
		text = aText;
	}
	
	public String toString() {
		return text;
	}
}
