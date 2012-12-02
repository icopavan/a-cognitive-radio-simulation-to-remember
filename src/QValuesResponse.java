
public enum QValuesResponse {

	DELETE_Q_VALUES("da"), 
	DELETE_OFFENDING_Q_VALUES("do"), 
	KEEP_Q_VALUES("ka");
	
	public String description;
	
	QValuesResponse(String aDescription) {
		description = aDescription;
	}
	
	@Override
	public String toString() {
		return description;
	}
		
}
