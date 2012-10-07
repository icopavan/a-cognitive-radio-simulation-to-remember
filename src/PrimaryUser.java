import java.util.Random;


public class PrimaryUser extends Agent {

	public String name;

	public static final double INITIAL_PROBABILITY_FOR_SWITCHING_CHANNELS = 0.5;
	public static final double MINIMUM_PROBABILITY_FOR_SWITCHING_CHANNELS = 0.05;
	double probabilityForSwitching;
	
	public PrimaryUser(String name, Environment environment, Spectrum initialSpectrum) {
		super(name, environment);
		this.name = name;
		currentState.spectrum = initialSpectrum;
		randomGenerator = new Random();
		iterationNumber = 0;
		probabilityForSwitching = INITIAL_PROBABILITY_FOR_SWITCHING_CHANNELS;
	}
	
	@Override
	public void chooseSpectrum() {
		super.chooseSpectrum();
		currentState.spectrum.containsPrimaryUser = true;
	}
	
	public void iterate() {
		iterationNumber++;
		if (role == Role.TRANSMITTER) {
			double randomDouble = randomGenerator.nextDouble();
			if (randomDouble < probabilityForSwitching) {
				if (debug) {
					FeliceUtil.log(name + " is changing spectrum.");
				}
				changeSpectrum();
				if (probabilityForSwitching > MINIMUM_PROBABILITY_FOR_SWITCHING_CHANNELS) {
					probabilityForSwitching -= MINIMUM_PROBABILITY_FOR_SWITCHING_CHANNELS;
				}
			}
		}
	}
	
	public void changeSpectrum() {
		int randomInt = randomGenerator.nextInt(environment.numberOfSpectra);
		Spectrum randomSpectrum = environment.spectrums.get(randomInt);
		// Check if the randomly chosen spectrum is equal to the current one
		if (!currentState.spectrum.equals(randomSpectrum)) {
			currentState.spectrum.containsPrimaryUser = false;
			peer.currentState.spectrum.containsPrimaryUser = false;
			currentState.spectrum = randomSpectrum;
			currentState.spectrum.containsPrimaryUser = true;
			peer.currentState.spectrum.containsPrimaryUser = true;
			return;
		} else {
			// Force re-selection
			changeSpectrum();
		}
	}
	
}
