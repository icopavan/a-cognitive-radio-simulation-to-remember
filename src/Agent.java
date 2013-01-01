import java.util.Random;

public class Agent {

	public static final String SETTINGS_FILE_NAME = "acrstr.conf";

	public State currentState;
	
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
		randomGenerator = new Random();
		getDebugSetting();
		iterationNumber = 0;
	}

	public void getDebugSetting() {
		debug = ACRSTRUtil.getSetting("debug").equals("true") ? true : false;
	}
		
	public void chooseSpectrum() {
		if (role == Role.TRANSMITTER) {
			int randomInt = randomGenerator.nextInt(environment.numberOfSpectra);
			currentState.spectrum = environment.spectrums.get(randomInt);
			if (debug) {
				ACRSTRUtil.log(name + " chose spectrum " + currentState.spectrum + ".");
			}
		} else {
			currentState.spectrum = peer.currentState.spectrum;
		}
	}
	
	public void transmit() {
		if (debug) {
			ACRSTRUtil.log(name + " is transmitting on " + currentState.spectrum);
		}
	}
	
	public void receive() {
		if (debug) {
			ACRSTRUtil.log(name + " is receiving on " + currentState.spectrum);
		}
	}
	
	public String toString() {
		return name;
	}
	
}
