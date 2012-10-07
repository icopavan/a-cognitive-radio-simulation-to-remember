import java.util.Random;

public class Agent {

	public static final String SETTINGS_FILE_NAME = "felice.conf";

	public State currentState;
	public State previousState;
	
	public String name;

	// The other agent paired with this agent
	public Agent peer;
	
	// Role of the agent; transmitter or receiver
	public Role role;
	
	// The environment associated with the agent
	public Environment environment;

	public int iterationNumber;
	
		// Debug flag
	public boolean debug;
	
	public Random randomGenerator;
	
	public Agent(String name, Environment environment) {
		this.name = name;
		this.environment = environment;
		currentState = new State();
		previousState = new State();
		randomGenerator = new Random();
		getDebugSetting();
		iterationNumber = 0;
	}

	public void getDebugSetting() {
		debug = FeliceUtil.getSetting("debug").equals("true") ? true : false;
	}
		
	public void chooseSpectrum() {
		if (role == Role.TRANSMITTER) {
			int randomInt = randomGenerator.nextInt(environment.numberOfSpectra);
			currentState.spectrum = environment.spectrums.get(randomInt);
			if (debug) {
				FeliceUtil.log(name + " chose spectrum " + currentState.spectrum + ".");
			}
		} else {
			currentState.spectrum = peer.currentState.spectrum;
		}
	}
	
	public void choosePower() {
		if (role == Role.TRANSMITTER) {
			int randomInt = randomGenerator.nextInt(environment.numberOfSpectra);
			currentState.power = environment.powerLevels[randomInt];
			if (debug) {
				FeliceUtil.log(name + " chose power level " + currentState.power + ".");
			}
		} else {
			currentState.power = peer.currentState.power;
		}

	}

	public void transmit() {
		if (debug) {
			FeliceUtil.log(name + " is transmitting on " + currentState.spectrum + ", with power level " + currentState.power);
		}
	}
	
	public void receive() {
		if (debug) {
			FeliceUtil.log(name + " is receiving on " + currentState.spectrum + ", with power level " + currentState.power);
		}
	}
	
	public String toString() {
		return name;
	}
	
}
