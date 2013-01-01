import java.io.IOException;


public class ACRSTRRunner {

	public static void main(String[] args) {
		System.out.println("Start of the simulation");
		ACRSTRSimulation aSimulation = new ACRSTRSimulation();
		try {
			aSimulation.startSimulation();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("End of the simulation");
	}
	
}
