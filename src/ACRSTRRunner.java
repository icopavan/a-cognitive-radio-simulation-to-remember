import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class ACRSTRRunner {

	public static void main(String[] args) {
		System.out.println("Start of the simulation");
		File oldOutput = new File(ACRSTRSimulation.DIRECTORY_FOR_LATEST_OUTPUT);
		if (oldOutput.exists()) {
			oldOutput.renameTo(new File("acrstr-"
					+ System.currentTimeMillis()));
		}
		
		ACRSTRSimulation simulation1 = new ACRSTRSimulation(Method.QLEARNING,
				0.0008, 5, QValuesResponse.DELETE_OBSOLETE_VALUES,
				RatesResponse.SET_TO_MIDPOINT, "blue");
		ACRSTRSimulation simulation2 = new ACRSTRSimulation(Method.QLEARNING,
				0.0008, 5, QValuesResponse.DELETE_OBSOLETE_VALUES,
				RatesResponse.INCREASE_BY_CONSTANT, "red");
		ACRSTRSimulation simulation3 = new ACRSTRSimulation(Method.QLEARNING,
				0.0008, 5, QValuesResponse.DELETE_OBSOLETE_VALUES,
				RatesResponse.RESET_TO_INITIAL_VALUES, "green");
		try {
			simulation1.startSimulation();
			simulation2.startSimulation();
			simulation3.startSimulation();	
			BufferedWriter infoBW = new BufferedWriter
					(new FileWriter(ACRSTRSimulation.DIRECTORY_FOR_LATEST_OUTPUT + "/info.ini"));
			infoBW.write(simulation1.parameters.toString() + "\n");
			infoBW.write(simulation2.parameters.toString() + "\n");
			infoBW.write(simulation3.parameters.toString() + "\n");
			infoBW.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("End of the simulation");
	}
	
}
