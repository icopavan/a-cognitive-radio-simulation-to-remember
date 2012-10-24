import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;


public class ACRSTRMain {
	
	public static boolean consoleDebug;
	public static boolean logging;
	public static int simulationNumber;
	
	public static int maximumPUPairs;
	
	public static ArrayList<PrimaryUser> puList;
	public static final int PU_PAIR_INTRODUCTION_EPOCH = 1000;
	
	public static final String DIRECTORY_FOR_LATEST_OUTPUT = "acrstr-latest";
	
	public static void main(String[] args) {
		System.out.println("Starting main method");
		ACRSTRUtil.initialize();
		puList = new ArrayList<PrimaryUser>();
		try {
			ACRSTRUtil.readSettingsFile();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		maximumPUPairs = Integer.parseInt(ACRSTRUtil.getSetting("primary-user-pairs"));
		consoleDebug = (ACRSTRUtil.getSetting("console-debug")).equals("true");
		logging = (ACRSTRUtil.getSetting("main-file-log")).equals(true);
		if (consoleDebug) {
			System.out.println("An Implementation of 'Spectrum Management of " +
					"Cognitive Radio Using Multi-agent Reinforcement Learning'\n");
		}
		try {
			if (logging) {
				ACRSTRUtil.log("###############");
			}
			System.out.println(String.format("Conducting simulation %s ...",
					simulationNumber));
			File oldOutput = new File(DIRECTORY_FOR_LATEST_OUTPUT);
			if (oldOutput.exists()) {
				oldOutput.renameTo(new File("acrstr-"
						+ System.currentTimeMillis()));
			}
			conductSimulation(Method.QLEARNING, simulationNumber,
					QValuesResponse.DELETE_Q_VALUES, RatesResponse.RESET_TO_INITIAL_VALUES);
			conductSimulation(Method.QLEARNING, simulationNumber,
					QValuesResponse.DELETE_OFFENDING_Q_VALUES, RatesResponse.RESET_TO_INITIAL_VALUES);
			conductSimulation(Method.QLEARNING, simulationNumber,
					QValuesResponse.KEEP_Q_VALUES, RatesResponse.RESET_TO_INITIAL_VALUES);
			conductSimulation(Method.QLEARNING, simulationNumber,
					QValuesResponse.DELETE_Q_VALUES, RatesResponse.INCREASE_BY_FACTOR);
			conductSimulation(Method.QLEARNING, simulationNumber,
					QValuesResponse.DELETE_OFFENDING_Q_VALUES, RatesResponse.INCREASE_BY_FACTOR);
			conductSimulation(Method.QLEARNING, simulationNumber,
					QValuesResponse.KEEP_Q_VALUES, RatesResponse.INCREASE_BY_FACTOR);
			conductSimulation(Method.QLEARNING, simulationNumber,
					QValuesResponse.DELETE_Q_VALUES, RatesResponse.INCREASE_BY_CONSTANT);
			conductSimulation(Method.QLEARNING, simulationNumber,
					QValuesResponse.DELETE_OFFENDING_Q_VALUES, RatesResponse.INCREASE_BY_CONSTANT);
			conductSimulation(Method.QLEARNING, simulationNumber,
					QValuesResponse.KEEP_Q_VALUES, RatesResponse.INCREASE_BY_CONSTANT);
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
	
	public static void conductSimulation(Method method, int checkLastNValues,
			QValuesResponse qValueResponse, RatesResponse ratesResponse)
			throws IOException {
		File outputDirectory = new File(DIRECTORY_FOR_LATEST_OUTPUT);
		outputDirectory.mkdir();
		String filename = DIRECTORY_FOR_LATEST_OUTPUT + '/' + System.currentTimeMillis() + ".txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("method", method.toString().toLowerCase());
		parameters.put("checked recent values", Integer.toString(checkLastNValues));
		parameters.put("q response", qValueResponse.toString());
		parameters.put("rate response", ratesResponse.toString());
		String jsonString = JSONValue.toJSONString(parameters);
		bw.write(jsonString + "\n"); 
		int numberOfPUPairs = 0;
		if (logging) {
			ACRSTRUtil.log("Conducting simulation for method: " + method + ".\n");
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
		
		int numberOfIterations = Integer.parseInt(ACRSTRUtil.getSetting("iterations"));
		
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

			double currentRewardAverage = currentRewardTotals
					/ (environment.numberOfSecondaryUsers / 2.0);
			bw.write(Double.toString(currentRewardAverage) + "\n");
		}

		if (logging) {
			ACRSTRUtil.log("\n=== Results ===");
			for (CognitiveRadio cr : environment.cognitiveRadios) {
				if (cr.role == Role.TRANSMITTER) {
					cr.printQ();
				}
			}
		}
		bw.close();
	}
	
}
