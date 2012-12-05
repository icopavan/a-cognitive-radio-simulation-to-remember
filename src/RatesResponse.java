
public enum RatesResponse {

	RESET_TO_INITIAL_VALUES("Reset to Initial Values"),
	SET_TO_MIDPOINT("Set to Midpoint Between Last and Initial Values"),
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
