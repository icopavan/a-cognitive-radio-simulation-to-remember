
public enum RatesResponse {

	RESET_TO_INITIAL_VALUES("ri"),
	SET_TO_MIDPOINT("stm"),
	INCREASE_BY_CONSTANT("ic");
	
	public String description;
	
	RatesResponse(String aDescription) {
		description = aDescription;
	}
	
	@Override
	public String toString() {
		return description;
	}
	
}
