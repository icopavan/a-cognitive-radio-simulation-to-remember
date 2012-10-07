import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;


public class FeliceMain {
	
	public static boolean consoleDebug;
	public static boolean logging;

	public static void main(String[] args) {
		System.out.println("Starting main method");
		FeliceUtil.initialize();
		try {
			FeliceUtil.readSettingsFile();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		consoleDebug = (FeliceUtil.getSetting("console-debug")).equals("true");
		logging = (FeliceUtil.getSetting("main-file-log")).equals(true);
		if (consoleDebug) {
			System.out.println("An Implementation of 'Spectrum Management of Cognitive Radio Using Multi-agent Reinforcement Learning'\n");
		}
		try {
			if (logging) {
				FeliceUtil.log("###############");
			}
			conductSimulation(Method.QLEARNING);
			conductSimulation(Method.RANDOM);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Ending main method");
	}
	
	public static void conductSimulation(Method method) throws IOException {
		if (logging) {
			FeliceUtil.log("Conducting simulation for method: " + method + ".\n");
		}
		
		Environment environment = new Environment();
		
		Random rand = new Random();
		
		int randomInt = rand.nextInt(environment.numberOfSpectra);
		PrimaryUser pu1 = new PrimaryUser("PU1", environment, environment.spectrums.get(randomInt));
		randomInt = rand.nextInt(environment.numberOfSpectra);
		PrimaryUser pu2 = new PrimaryUser("PU2", environment, environment.spectrums.get(randomInt));
		
		pu1.role = Role.TRANSMITTER;
		pu2.role = Role.RECEIVER;
		
		pu1.peer = pu2;
		pu2.peer = pu1;
		
		pu1.choosePower();
		pu1.chooseSpectrum();
		pu1.transmit();
		
		for (int i = 0; i < environment.numberOfSecondaryUsers; i++) {
			environment.cognitiveRadios.add(new CognitiveRadio("CR" + (i + 1), environment, method));
		}
		
		int currentCR = 0;
		
		for (CognitiveRadio cr : environment.cognitiveRadios) {
			if (currentCR % 2 == 0) {
				cr.role = Role.TRANSMITTER;
				cr.peer = environment.cognitiveRadios.get(currentCR + 1);
			} else {
				cr.role = Role.RECEIVER;
				cr.peer = environment.cognitiveRadios.get(currentCR - 1);
			}
			currentCR++;
		}
		
		for (CognitiveRadio cr: environment.cognitiveRadios) {
			cr.initializeParameters();
		}
		
		int numberOfIterations = Integer.parseInt(FeliceUtil.getSetting("iterations"));
		
		for (int i = 0; i < numberOfIterations; i++) {
			if (consoleDebug) {
				System.out.println("Iteration: " + (i + 1));
			}
			double currentRewardTotals = 0.0;
			
			for (CognitiveRadio cr : environment.cognitiveRadios) {
				cr.iterate();
			}
			
			int channelSwitches = 0;
			int successfulTransmissions = 0;
			for (CognitiveRadio cr : environment.cognitiveRadios) {
				if (cr.role == Role.TRANSMITTER) {
					cr.evaluate();
					if (cr.changedChannelThisIteration) {
						channelSwitches++;
					}
					if (cr.succesfullyTransmittedThisEpoch) {
						successfulTransmissions++;
					}
					currentRewardTotals += cr.currentIterationsReward;
				}
			}
			double successfulTransmissionProbability = (double) successfulTransmissions
					/ (environment.numberOfSecondaryUsers / 2.0);
			
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(method.toString().toLowerCase()
						+ "_channel_switches.txt", true));
				bw.write(channelSwitches + "\n");
				bw.close();
				bw = new BufferedWriter(new FileWriter(method.toString().toLowerCase()
						+ "_successful_transmission.txt", true));
				bw.write(successfulTransmissionProbability + "\n");
				bw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			for (Spectrum s : environment.spectrums) {
				s.occupyingAgents.clear();
			}
			
			double currentRewardAverage = currentRewardTotals / (environment.numberOfSecondaryUsers / 2.0);
			BufferedWriter bw = new BufferedWriter(new FileWriter(method.toString().toLowerCase()
					+ "_average_rewards.txt", true));
			bw.write(currentRewardAverage + "\n");
			bw.close();
			pu1.iterate();
			if (logging) {
				FeliceUtil.log("Iteration: " + (i + 1) + "\nPrimary user is on channel "
						+ pu1.currentState.spectrum);
			}
		}

		if (logging) {
			FeliceUtil.log("\n=== Results ===");
			for (CognitiveRadio cr : environment.cognitiveRadios) {
				if (cr.role == Role.TRANSMITTER) {
					cr.printQ();
				}
			}
		}
		
	}
	
}
