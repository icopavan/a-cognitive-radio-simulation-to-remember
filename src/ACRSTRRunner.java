import java.io.IOException;


public class ACRSTRRunner {

	public static void main(String[] args) {
		System.out.println("Start of the simulation");
		ACRSTRSimulation aSimulation = new ACRSTRSimulation(Method.RANDOM,
				0.0008, 5, QValuesResponse.DELETE_OBSOLETE_VALUES,
				RatesResponse.SET_TO_MIDPOINT, "blue");
		try {
			aSimulation.startSimulation();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("End of the simulation");
	}
	
}
