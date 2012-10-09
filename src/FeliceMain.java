import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class FeliceMain {
	
	public static boolean consoleDebug;
	public static boolean logging;

	public static final int PU_PAIR_INTRODUCTION_EPOCH = 2000;
	
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
			conductSimulation(Method.QLEARNING, true);
			conductSimulation(Method.RANDOM, true);
			conductSimulation(Method.QLEARNING, false);
			conductSimulation(Method.RANDOM, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Ending main method");
	}
	
	public static void introduceAPUPair(String transmitterName, String receiverName, Environment anEnvironment, Spectrum aSpectrum) {
		PrimaryUser transmitterPU = new PrimaryUser(transmitterName, anEnvironment);
		PrimaryUser receiverPU = new PrimaryUser(receiverName, anEnvironment);
		
		transmitterPU.role = Role.TRANSMITTER;
		receiverPU.role = Role.RECEIVER;
		
		transmitterPU.peer = receiverPU;
		receiverPU.peer = transmitterPU;
		
		transmitterPU.choosePrimarySpectrum(aSpectrum);
		receiverPU.choosePrimarySpectrum(aSpectrum);
		
		PrimaryUser[] puPair = new PrimaryUser[]{ transmitterPU, receiverPU };
		anEnvironment.primaryUserPairs.add(puPair);
	}
	
	public static void conductSimulation(Method method, boolean evaluateResults) throws IOException {
		if (logging) {
			FeliceUtil.log("Conducting simulation for method: " + method + ".\n");
		}
		
		Environment environment = new Environment();
		
		for (int i = 0; i < environment.numberOfSecondaryUsers; i++) {
			environment.cognitiveRadios.add(new CognitiveRadio("CR" + (i + 1), environment, method,
					evaluateResults));
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
			
			if (i % PU_PAIR_INTRODUCTION_EPOCH == 0) {
				String firstPUName = String.format("PU%s", i / PU_PAIR_INTRODUCTION_EPOCH + 1);
				String secondPUName = String.format("PU%s", i / PU_PAIR_INTRODUCTION_EPOCH + 2);
				introduceAPUPair(firstPUName, secondPUName, environment, environment.spectrums.get(i / PU_PAIR_INTRODUCTION_EPOCH));
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
					if (cr.succesfullyTransmittedThisIteration) {
						successfulTransmissions++;
					}
					currentRewardTotals += cr.currentIterationsReward;
				}
			}
			double successfulTransmissionProbability = (double) successfulTransmissions
					/ (environment.numberOfSecondaryUsers / 2.0);
			String evaluationValue = evaluateResults ? "with" : "without";
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(method.toString().toLowerCase()
						+ String.format("_channel_switches.txt", evaluationValue), true));
				bw.write(channelSwitches + "\n");
				bw.close();
				bw = new BufferedWriter(new FileWriter(method.toString().toLowerCase()
						+ String.format("_successful_transmission.txt", evaluationValue), true));
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
					+ String.format("_average_rewards_%s_evaluation.txt", evaluationValue), true));
			bw.write(currentRewardAverage + "\n");
			bw.close();
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
