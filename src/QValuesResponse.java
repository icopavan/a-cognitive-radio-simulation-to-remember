
public enum QValuesResponse {

	DELETE_Q_VALUES("Delete all Q values"), 
	DELETE_OFFENDING_Q_VALUES("Only delete offending Q values"), 
	KEEP_Q_VALUES("Do not delete any Q values");
	
	public String description;
	
	QValuesResponse(String aDescription) {
		description = aDescription;
	}
	
	@Override
	public String toString() {
		return description;
	}
		
}
