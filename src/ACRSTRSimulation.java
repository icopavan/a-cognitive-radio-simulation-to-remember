import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONValue;


public class ACRSTRSimulation {

	public static int NUMBER_OF_SECONDARY_USERS = 4;
	public static int TAKE_AVERAGE_OF_N_VALUES = 100;
	public static int[] EPOCHS_TO_ACTIVATE_PU_PAIRS = { 0 };
	public static int[] EPOCHS_TO_DEACTIVATE_PU_PAIRS = { };
	public static String X_AXIS_LABEL = "Iteration";
	public static final String DIRECTORY_FOR_LATEST_OUTPUT = "acrstr-latest";
	
	public Double epsilonDecrement;
	public Environment environment;	
	public Integer lastValuesToCheck;
	public Map<String, String> parameters;
	public Method methodToSimulate;
	public QValuesResponse qValuesResponse;
	public RatesResponse ratesResponse;
	public Random randomNumberGenerator;
	public String color;
	
	public ACRSTRSimulation(Method aMethod, double anEpsilonDecrement,
			Integer aLastValuesToCheck, QValuesResponse aQValuesResponse,
			RatesResponse aRatesResponse, String aColor) {
		methodToSimulate = aMethod;
		epsilonDecrement = new Double(anEpsilonDecrement);
		lastValuesToCheck = new Integer(aLastValuesToCheck);
		qValuesResponse = aQValuesResponse;
		ratesResponse = aRatesResponse;
		color = aColor;
	}
	
	public void startSimulation() throws IOException {
		environment = new Environment(NUMBER_OF_SECONDARY_USERS);
		randomNumberGenerator = new Random();
				
		ACRSTRUtil.initialize();
		ACRSTRUtil.readSettingsFile();

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
		
		File outputDirectory = new File(DIRECTORY_FOR_LATEST_OUTPUT);
		outputDirectory.mkdir();
		
		int numberOfIterations = Integer.parseInt(ACRSTRUtil.getSetting("iterations"));
		
		String filename = DIRECTORY_FOR_LATEST_OUTPUT + '/' + System.currentTimeMillis() + ".txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		parameters = new HashMap<String, String>();
		parameters.put("method", method.toString());
		parameters.put("evaluation", lastValuesToCheck.toString());
		parameters.put("q-response", qValueResponse.toString());
		parameters.put("rate-response", ratesResponse.toString());
		parameters.put("color", color);
		parameters.put("epsilon-decrement", epsilonDecrement.toString());
		String compared = ACRSTRUtil.getSetting("compare");
		String instance = parameters.get(compared);
		parameters.put("legend", getLegend(compared, instance));
		parameters.put("xLabel", X_AXIS_LABEL);
		parameters.put("yLabel", getYLabel(output));
			
		int numberOfLines = output.startsWith("average-")
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

			for (CognitiveRadio cr : environment.cognitiveRadios) {
				cr.evaluate();
				currentRewardTotals += cr.currentIterationsReward;
				if (cr.successfullyTransmittedThisIteration) {
					numberOfSuccessfulTransmissionsThisIteration++;
				}
			}

			double probabilityOfSuccessfulTransmission = (double) numberOfSuccessfulTransmissionsThisIteration
					/ environment.numberOfSecondaryUsers;

			double currentRewardAverage = currentRewardTotals
					/ environment.numberOfSecondaryUsers;
			if (output.equals("reward")) {
				bw.write(Double.toString(currentRewardAverage) + "\n");	
			} else if (output.equals("probability")) {
				bw.write(Double.toString(probabilityOfSuccessfulTransmission)
						+ "\n");
			} else if (output.equals("average-reward")) {
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
			} else if (output.equals("average-probability")) {
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
			}
		}
		bw.close();
	}
	
	public String getYLabel(String output) {
		if (output.contains("reward")) {
			return "Average Rewards";
		} else if (output.contains("probability")) {
			return "Probability of Successful Transmission";
		}
		return "Unknown";
	}
	
	public String getLegend(String compared, String instance) {
		if (compared.equals("method")) {
			return instance;
		} else if (compared.equals("evaluation")) {
			int checkedLastValues = Integer.parseInt(instance);
			if (checkedLastValues > 0) {
				return String.format("Evaluate Last %d values", checkedLastValues);
			} else {
				return "No Self Evaluation";
			}
		}
		return "Unknown";
	}

}
