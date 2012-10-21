import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class FeliceMain {
	
	public static boolean consoleDebug;
	public static boolean logging;
	public static int simulationNumber;
	
	public static int maximumPUPairs;
	
	public static ArrayList<PrimaryUser> puList;
	public static final int PU_PAIR_INTRODUCTION_EPOCH = 1000;
	
	public static final int START_VALUE = 5;
	public static final int END_VALUE = 5;
	
	public static void main(String[] args) {
		System.out.println("Starting main method");
		FeliceUtil.initialize();
		puList = new ArrayList<PrimaryUser>();
		try {
			FeliceUtil.readSettingsFile();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		maximumPUPairs = Integer.parseInt(FeliceUtil.getSetting("primary-user-pairs"));
		consoleDebug = (FeliceUtil.getSetting("console-debug")).equals("true");
		logging = (FeliceUtil.getSetting("main-file-log")).equals(true);
		if (consoleDebug) {
			System.out.println("An Implementation of 'Spectrum Management of Cognitive Radio Using Multi-agent Reinforcement Learning'\n");
		}
		try {
			if (logging) {
				FeliceUtil.log("###############");
			}
			for (simulationNumber = START_VALUE; simulationNumber <= END_VALUE;
					simulationNumber++) {
				System.out.println(String.format("Conducting simulation %s ...",
						simulationNumber));
				conductSimulation(Method.QLEARNING, simulationNumber,
						QValuesResponse.DELETE_Q_VALUES, RatesResponse.RESET_TO_INITIAL_VALUES);
				conductSimulation(Method.QLEARNING, simulationNumber,
						QValuesResponse.DELETE_OFFENDING_Q_VALUES, RatesResponse.RESET_TO_INITIAL_VALUES);
				conductSimulation(Method.QLEARNING, simulationNumber, QValuesResponse.KEEP_Q_VALUES, RatesResponse.RESET_TO_INITIAL_VALUES);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Ending main method");
	}
	
	public static void introduceAPUPair(String transmitterName, String receiverName,
			Environment anEnvironment, Spectrum aSpectrum) {
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
		puList.add(transmitterPU);
		puList.add(receiverPU);
	}
	
	public static String getLowerCaseEnumName(@SuppressWarnings("rawtypes") Enum anEnum) {
		return anEnum.toString().toLowerCase();
	}
	
	public static void conductSimulation(Method method, int checkLastNValues, QValuesResponse qValueResponse, RatesResponse ratesResponse)
			throws IOException {
		String filename = String.format("%s-%s-%s-%s",
				getLowerCaseEnumName(method), checkLastNValues,
				getLowerCaseEnumName(qValueResponse),
				getLowerCaseEnumName(ratesResponse));
		int numberOfPUPairs = 0;
		if (logging) {
			FeliceUtil.log("Conducting simulation for method: " + method + ".\n");
		}
		
		Environment environment = new Environment();
		
		for (int i = 0; i < environment.numberOfSecondaryUsers; i++) {
			environment.cognitiveRadios.add(new CognitiveRadio("CR" + (i + 1), environment, method,
					checkLastNValues, qValueResponse, ratesResponse));
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
			
			if (i % PU_PAIR_INTRODUCTION_EPOCH == 0 && numberOfPUPairs <= maximumPUPairs) {
				String firstPUName = String.format("PU%s", i /
						PU_PAIR_INTRODUCTION_EPOCH + 1);
				String secondPUName = String.format("PU%s", i /
						PU_PAIR_INTRODUCTION_EPOCH + 2);
				introduceAPUPair(firstPUName, secondPUName, environment,
						environment.spectrums.get((i / PU_PAIR_INTRODUCTION_EPOCH)
								% environment.numberOfSpectra));
				numberOfPUPairs++;
			}
			
			double currentRewardTotals = 0.0;
		
			for (PrimaryUser pu : puList) {
				pu.iterate();
			}
			
			for (CognitiveRadio cr : environment.cognitiveRadios) {
				cr.iterate();
			}
			
			for (CognitiveRadio cr : environment.cognitiveRadios) {
				if (cr.role == Role.TRANSMITTER) {
					cr.evaluate();
					currentRewardTotals += cr.currentIterationsReward;
				}
			}
			for (Spectrum s : environment.spectrums) {
				s.occupyingAgents.clear();
			}
			
			double currentRewardAverage = currentRewardTotals / (environment.numberOfSecondaryUsers / 2.0);
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename + "_reward_average.txt", true));
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
