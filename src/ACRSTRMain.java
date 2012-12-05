import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.json.simple.JSONValue;


public class ACRSTRMain {
	
	public static boolean consoleDebug;
	public static boolean logging;
	
	public static int maximumPUPairs;
	
	public static ArrayList<PrimaryUser> puList;
	public static final int PU_PAIR_INTRODUCTION_EPOCH = 1000;
	
	public static final String DIRECTORY_FOR_LATEST_OUTPUT = "acrstr-latest";
	
	public static final int START_N_VALUES = 0;
	public static final int END_N_VALUES = 0;
	
	public static List<QValuesResponse> qValuesResponses;
	public static List<RatesResponse> ratesResponses;
	public static List<Method> methodsToSimulate;
	
	public static int numberOfCRTransmitters;
	public static Stack<String> colors;
	public static Stack<String> zOrders;
	
	public static void main(String[] args) {
		colors = new Stack<String>();
		colors.push("red");
		colors.push("blue");
		
		zOrders = new Stack<String>();
		zOrders.push("1");
		zOrders.push("2");
		
		qValuesResponses = new ArrayList<QValuesResponse>();
		ratesResponses = new ArrayList<RatesResponse>();
		methodsToSimulate = new ArrayList<Method>();
		qValuesResponses.add(QValuesResponse.KEEP_Q_VALUES);
		ratesResponses.add(RatesResponse.RESET_TO_INITIAL_VALUES);
		methodsToSimulate.add(Method.QLEARNING);
		methodsToSimulate.add(Method.RANDOM);
		
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
			File oldOutput = new File(DIRECTORY_FOR_LATEST_OUTPUT);
			if (oldOutput.exists()) {
				oldOutput.renameTo(new File("acrstr-"
						+ System.currentTimeMillis()));
			}
			
			String output = ACRSTRUtil.getSetting("output");
			
			for (QValuesResponse qvr : qValuesResponses) {
				for (RatesResponse rsr : ratesResponses) {
					for (Method m : methodsToSimulate) {
						conductSimulation(m, START_N_VALUES, END_N_VALUES, qvr, rsr, output);
					}
				}
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
	
	public static void conductSimulation(Method method, int startNValues,
			int endNValues, QValuesResponse qValueResponse,
			RatesResponse ratesResponse, String output)
			throws IOException {
		File outputDirectory = new File(DIRECTORY_FOR_LATEST_OUTPUT);
		outputDirectory.mkdir();
		
		for (int checkLastNValues = startNValues; checkLastNValues <= endNValues; checkLastNValues++) {
			System.out.println(String.format("Checking last %s values ...", checkLastNValues));
			
			String filename = DIRECTORY_FOR_LATEST_OUTPUT + '/' + System.currentTimeMillis() + ".txt";
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("method", method.toString());
			parameters.put("checked recent values", Integer.toString(checkLastNValues));
			parameters.put("q response", qValueResponse.toString());
			parameters.put("rate response", ratesResponse.toString());
			parameters.put("color", colors.pop());
			parameters.put("zOrder", zOrders.pop());
			parameters.put("comparing", parameters.get(ACRSTRUtil.getSetting("compare")));
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
			
			numberOfCRTransmitters = environment.numberOfSecondaryUsers / 2;

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

			double cumulativeRewards = 0.0;
			
			double cumulativeSuccessProbabilities = 0.0;
			
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

				int numberOfSuccessfulTransmissionsThisIteration = 0;
				int channelChangesThisIteration = 0;
				
				for (CognitiveRadio cr : environment.cognitiveRadios) {
					if (cr.role == Role.TRANSMITTER) {
						cr.evaluate();
						currentRewardTotals += cr.currentIterationsReward;
						if (cr.succesfullyTransmittedThisIteration) {
							numberOfSuccessfulTransmissionsThisIteration++;
						}
						if (cr.changedChannelThisIteration) {
							channelChangesThisIteration++;
						}
					}
				}
				
				cumulativeRewards += currentRewardTotals;
				double cumulativeRewardAverage = cumulativeRewards / (i + 1);

				double probabilityOfSuccessfulTransmission = (double) numberOfSuccessfulTransmissionsThisIteration
						/ numberOfCRTransmitters;
				
				cumulativeSuccessProbabilities += probabilityOfSuccessfulTransmission
						/ ((i + 1) * numberOfCRTransmitters) ;
				
				for (Spectrum s : environment.spectrums) {
					s.occupyingAgents.clear();
				}

				double currentRewardAverage = currentRewardTotals
						/ numberOfCRTransmitters;
				if (output.equals("average")) {
					bw.write(Double.toString(currentRewardAverage) + "\n");	
				} else if (output.equals("probability")) {
					bw.write(Double.toString(probabilityOfSuccessfulTransmission)
							+ "\n");
				} else if (output.equals("cumulative-reward")) {
					bw.write(Double.toString(cumulativeRewardAverage) + "\n");
				} else if (output.equals("cumulative-probability")) {
					bw.write(Double.toString(cumulativeSuccessProbabilities) + "\n");
				} else if (output.equals("channel-changes")) {
					bw.write(Integer.toString(channelChangesThisIteration) + "\n");
				}		
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

}
