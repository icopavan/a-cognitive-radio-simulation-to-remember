import java.io.IOException;
import java.util.Random;


public class PrimaryUser extends Agent {

	public String name;

	public PrimaryUser(String name, Environment environment) {
		super(name, environment);
		this.name = name;
		randomGenerator = new Random();
		iterationNumber = 0;
	}
	
	public void choosePrimarySpectrum(Spectrum aSpectrum) {
		currentState.spectrum = aSpectrum;
		currentState.spectrum.containsPrimaryUser = true;
	}
	
	public void iterate() {
		if (role == Role.TRANSMITTER) {
			transmit();
			iterationNumber++;
			try {
				FeliceUtil.createOutputInAppendMode(name + ".txt", name
						+ "," + currentState.spectrum.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
