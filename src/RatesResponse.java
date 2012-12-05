
public enum RatesResponse {

	RESET_TO_INITIAL_VALUES("Reset to Initial Values"),
	SET_TO_MIDPOINT("Set to Midpoint Between Initial and Last Epsilon"),
	INCREASE_BY_CONSTANT("Increase by a Constant");
	
	public String description;
	
	RatesResponse(String aDescription) {
		description = aDescription;
	}
	
	@Override
	public String toString() {
		return description;
	}
	
}
