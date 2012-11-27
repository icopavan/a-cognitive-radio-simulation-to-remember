
public enum QValuesResponse {

	DELETE_Q_VALUES("Delete All Values"), 
	DELETE_OFFENDING_Q_VALUES("Delete Obsolete Values"), 
	KEEP_Q_VALUES("Keep All Values");
	
	public String description;
	
	QValuesResponse(String aDescription) {
		description = aDescription;
	}
	
	@Override
	public String toString() {
		return description;
	}
		
}
