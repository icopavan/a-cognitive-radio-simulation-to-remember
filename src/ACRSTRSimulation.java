import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import org.json.simple.JSONValue;


public class ACRSTRSimulation {

	public static int[] EPOCHS_TO_ACTIVATE_PU_PAIRS = { 0, 5, 10, 15 };
	public static int[] EPOCHS_TO_DEACTIVATE_PU_PAIRS = { 6000, 8000 };
	public static int NUMBER_OF_SECONDARY_USERS = 1;

	public static int TAKE_AVERAGE_OF_N_VALUES = 50;
	
	public static ArrayList<PrimaryUser> puList;
		
	public static final String DIRECTORY_FOR_LATEST_OUTPUT = "acrstr-latest";
	
	public static final int START_N_VALUES = 0;
	public static final int END_N_VALUES = 0;
	
	public QValuesResponse qValuesResponse;
	public RatesResponse ratesResponse;
	public Method methodToSimulate;
	public Integer lastValuesToCheck;
	public Double epsilonDecrement;
	
	public static Stack<String> colors;
	
	public Environment environment;
	public Random randomNumberGenerator;
	
	public ACRSTRSimulation(Method aMethod, double anEpsilonDecrement,
			Integer aLastValuesToCheck, QValuesResponse aQValuesResponse,
			RatesResponse aRatesResponse) {
		methodToSimulate = aMethod;
		epsilonDecrement = new Double(anEpsilonDecrement);
		lastValuesToCheck = new Integer(aLastValuesToCheck);
		qValuesResponse = aQValuesResponse;
		ratesResponse = aRatesResponse;
	}
	
	public void startSimulation() throws IOException {
		environment = new Environment(NUMBER_OF_SECONDARY_USERS);
		randomNumberGenerator = new Random();
		colors = new Stack<String>();
		colors.push("blue");
		colors.push("red");
				
		ACRSTRUtil.initialize();
		try {
			ACRSTRUtil.readSettingsFile();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		File oldOutput = new File(DIRECTORY_FOR_LATEST_OUTPUT);
		if (oldOutput.exists()) {
			oldOutput.renameTo(new File("acrstr-"
					+ System.currentTimeMillis()));
		}
		String output = ACRSTRUtil.getSetting("output");
		conductSimulation(methodToSimulate, qValuesResponse, ratesResponse,
				output, epsilonDecrement, lastValuesToCheck);

	}
	
	public void conductSimulation(Method method, QValuesResponse qValueResponse,
			RatesResponse ratesResponse, String output, Double epsilonDecrement,
			Integer lastValuesToCheck)
			throws IOException {
		List<Double> lastNAverages = new ArrayList<Double>();
		List<Double> lastNProbabilities = new ArrayList<Double>();
		List<Integer> lastNChannelChanges = new ArrayList<Integer>();
		
		File outputDirectory = new File(DIRECTORY_FOR_LATEST_OUTPUT);
		outputDirectory.mkdir();
		
		int numberOfIterations = Integer.parseInt(ACRSTRUtil.getSetting("iterations"));
		
		String filename = DIRECTORY_FOR_LATEST_OUTPUT + '/' + System.currentTimeMillis() + ".txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("method", method.toString());
		parameters.put("checked recent values", lastValuesToCheck.toString());
		parameters.put("q response", qValueResponse.toString());
		parameters.put("rate response", ratesResponse.toString());
		parameters.put("color", colors.pop());
		parameters.put("epsilon decrement", epsilonDecrement.toString());
		parameters.put("comparing", parameters.get(ACRSTRUtil.getSetting("compare")));
		parameters.put("xLabel", ACRSTRUtil.getSetting("x-label"));
		parameters.put("yLabel", ACRSTRUtil.getSetting("y-label"));
			
		int numberOfLines = output.startsWith("average-of-last-n-")
				? numberOfIterations / TAKE_AVERAGE_OF_N_VALUES : numberOfIterations;
			
		parameters.put("numberOfValues", Integer.toString(numberOfLines)); 

		String jsonString = JSONValue.toJSONString(parameters);
		bw.write(jsonString + "\n"); 

		for (int i = 0; i < environment.numberOfSecondaryUsers; i++) {
			environment.cognitiveRadios.add(new CognitiveRadio("CR" + (i + 1),
					environment, method, lastValuesToCheck, qValueResponse,
					ratesResponse, epsilonDecrement));
		}
			
		for (int i = 0; i < numberOfIterations; i++) {
			int index = 0;
			for (int activationEpoch : EPOCHS_TO_ACTIVATE_PU_PAIRS) {
				if (i == activationEpoch) {
					String puName = String.format("PU%s", index + 1);
					environment.introduceAPU(puName);
				}
				index++;
			}

			for (int deactivationEpoch : EPOCHS_TO_DEACTIVATE_PU_PAIRS) {
				if (i == deactivationEpoch) {
					environment.deactivateAPU();
				}
			}

			double currentRewardTotals = 0.0;

			for (CognitiveRadio cr : environment.cognitiveRadios) {
				cr.act();
			}

			int numberOfSuccessfulTransmissionsThisIteration = 0;
			int channelChangesThisIteration = 0;

			for (CognitiveRadio cr : environment.cognitiveRadios) {
				cr.evaluate();
				currentRewardTotals += cr.currentIterationsReward;
				if (cr.successfullyTransmittedThisIteration) {
					numberOfSuccessfulTransmissionsThisIteration++;
				}
				if (cr.changedChannelThisIteration) {
					channelChangesThisIteration++;
				}
			}

			double probabilityOfSuccessfulTransmission = (double) numberOfSuccessfulTransmissionsThisIteration
					/ environment.numberOfSecondaryUsers;

			for (Spectrum s : environment.spectra) {
				s.occupyingAgents.clear();
			}

			double currentRewardAverage = currentRewardTotals
					/ environment.numberOfSecondaryUsers;
			if (output.equals("average")) {
				bw.write(Double.toString(currentRewardAverage) + "\n");	
			} else if (output.equals("probability")) {
				bw.write(Double.toString(probabilityOfSuccessfulTransmission)
						+ "\n");
			} else if (output.equals("channel-changes")) {
				bw.write(Integer.toString(channelChangesThisIteration) + "\n");
			} else if (output.equals("average-of-last-n-rewards")) {
				if (i % TAKE_AVERAGE_OF_N_VALUES == 0) {
					double sumOfLastNValues = 0.0;
					for (Double value : lastNAverages) {
						sumOfLastNValues += value;
					}
					double average = sumOfLastNValues / TAKE_AVERAGE_OF_N_VALUES;
					bw.write(Double.toString(average) + "\n");
					lastNAverages.clear();
				} else {
					lastNAverages.add(currentRewardAverage);
				}
			} else if (output.equals("average-of-last-n-probabilities")) {
				if (i % TAKE_AVERAGE_OF_N_VALUES == 0) {
					double sumOfLastNValues = 0.0;
					for (Double value : lastNProbabilities) {
						sumOfLastNValues += value;
					}
					double average = sumOfLastNValues / TAKE_AVERAGE_OF_N_VALUES;
					bw.write(Double.toString(average) + "\n");
					lastNProbabilities.clear();
				} else {
					lastNProbabilities.add(probabilityOfSuccessfulTransmission);
				}

			} else if (output.equals("average-of-last-n-channel-changes")) {
				if (i % TAKE_AVERAGE_OF_N_VALUES == 0) {
					double sumOfLastNValues = 0.0;
					for (Integer value : lastNChannelChanges) {
						sumOfLastNValues += value;
					}
					double average = (double) sumOfLastNValues / TAKE_AVERAGE_OF_N_VALUES;
					bw.write(Double.toString(average) + "\n");
					lastNChannelChanges.clear();
				} else {
					lastNChannelChanges.add(channelChangesThisIteration);
				}

			} 
		}

		bw.close();

	}

}
