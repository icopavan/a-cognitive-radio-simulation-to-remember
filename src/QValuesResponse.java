
public enum QValuesResponse {

	DELETE_ALL_VALUES("Delete All Values"), 
	DELETE_OBSOLETE_VALUES("Delete Obsolete Values"), 
	KEEP_ALL_VALUES("Keep All Values");
	
	public String description;
	
	QValuesResponse(String aDescription) {
		description = aDescription;
	}
	
	@Override
	public String toString() {
		return description;
	}
		
}
