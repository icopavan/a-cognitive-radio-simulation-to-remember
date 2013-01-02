import java.io.File;
import java.io.IOException;


public class ACRSTRRunner {

	public static void main(String[] args) {
		System.out.println("Start of the simulation");
		File oldOutput = new File(ACRSTRSimulation.DIRECTORY_FOR_LATEST_OUTPUT);
		if (oldOutput.exists()) {
			oldOutput.renameTo(new File("acrstr-"
					+ System.currentTimeMillis()));
		}
		ACRSTRSimulation qLearningSimulation = new ACRSTRSimulation(Method.QLEARNING,
				0.0008, 5, QValuesResponse.DELETE_OBSOLETE_VALUES,
				RatesResponse.SET_TO_MIDPOINT, "blue");
		ACRSTRSimulation randomSimulation = new ACRSTRSimulation(Method.RANDOM,
				0.0008, 5, QValuesResponse.DELETE_OBSOLETE_VALUES,
				RatesResponse.SET_TO_MIDPOINT, "red");
		try {
			qLearningSimulation.startSimulation();
			randomSimulation.startSimulation();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("End of the simulation");
	}
	
}
