
public enum RatesResponse {

	RESET_TO_INITIAL_VALUES("Reset to initial values"),
	INCREASE_BY_FACTOR("Increase by a factor"),
	INCREASE_BY_CONSTANT("Increase by a constant");
	
	public String description;
	
	RatesResponse(String aDescription) {
		description = aDescription;
	}
	
	@Override
	public String toString() {
		return description;
	}
	
}
