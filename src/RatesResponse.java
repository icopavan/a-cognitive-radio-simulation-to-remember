
public enum RatesResponse {

	RESET_TO_INITIAL_VALUES("Reset to Initial Values"),
	INCREASE_BY_FACTOR("Multiply By a Factor"),
	INCREASE_BY_CONSTANT("Increase By a Constant");
	
	public String description;
	
	RatesResponse(String aDescription) {
		description = aDescription;
	}
	
	@Override
	public String toString() {
		return description;
	}
	
}
