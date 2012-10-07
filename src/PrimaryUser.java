import java.util.Random;


public class PrimaryUser extends Agent {

	public String name;

	public PrimaryUser(String name, Environment environment) {
		super(name, environment);
		this.name = name;
		randomGenerator = new Random();
		iterationNumber = 0;
	}
	
	@Override
	public void chooseSpectrum() {
		super.chooseSpectrum();
		currentState.spectrum.containsPrimaryUser = true;
	}
	
	public void iterate() {
		iterationNumber++;
		if (role == Role.TRANSMITTER) {
			transmit();
			iterationNumber++;
		}
	}
	
}
