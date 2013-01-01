import java.io.IOException;


public class ACRSTRRunner {

	public static void main(String[] args) {
		ACRSTRSimulation aSimulation = new ACRSTRSimulation();
		try {
			aSimulation.startSimulation();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
