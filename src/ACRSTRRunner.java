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
				0.0008, 0, QValuesResponse.DELETE_OBSOLETE_VALUES,
				RatesResponse.SET_TO_MIDPOINT, "blue");
		try {
			simulation1.startSimulation();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			BufferedWriter infoBW = new BufferedWriter
					(new FileWriter(ACRSTRSimulation.DIRECTORY_FOR_LATEST_OUTPUT + "/info.ini"));
			infoBW.write(simulation1.parameters.toString() + "\n");
			infoBW.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("End of the simulation");
	}
	
}
