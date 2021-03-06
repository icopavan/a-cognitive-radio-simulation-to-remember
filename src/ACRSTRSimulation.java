import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONValue;

public class ACRSTRSimulation {

	public static int TAKE_AVERAGE_OF_N_VALUES = 100;
	public static int[] EPOCHS_TO_INTRODUCE_PUS = { };
	public static int[] EPOCHS_TO_DEACTIVATE_PUS = { };
	public static String X_AXIS_LABEL = "Iteration";
	public static final String DIRECTORY_FOR_LATEST_OUTPUT = "acrstr-latest";

	public double greedyExploration;
	public int numberOfSecondaryUsers;
	public int numberOfSpectra;
	public int numberOfPrimaryUsers;
	public Double epsilonDecrement;
	public Environment environment;
	public Integer lastValuesToCheck;
	public Map<String, String> parameters;
	public Method methodToSimulate;
	public QValuesResponse qValuesResponse;
	public RatesResponse ratesResponse;
	public Random randomNumberGenerator;
	public String color;
	public String name;

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
		ACRSTRUtil.initialize();
		ACRSTRUtil.readSettingsFile();

		numberOfSecondaryUsers = new Integer(ACRSTRUtil.getSetting("secondary-users"));
		numberOfSpectra = new Integer(ACRSTRUtil.getSetting("spectra-number"));
		numberOfPrimaryUsers = new Integer(ACRSTRUtil.getSetting("primary-users"));
		environment = new Environment(numberOfSecondaryUsers, numberOfSpectra);
		randomNumberGenerator = new Random();
		greedyExploration = Double.parseDouble((ACRSTRUtil.getSetting("greedy-exploration")));
		name = ACRSTRUtil.getSetting("name");
		String output = ACRSTRUtil.getSetting("output");
		conductSimulation(methodToSimulate, qValuesResponse, ratesResponse,
				output, epsilonDecrement, lastValuesToCheck, greedyExploration);
	}

	public void conductSimulation(Method method,
			QValuesResponse qValueResponse, RatesResponse ratesResponse,
			String output, Double epsilonDecrement, Integer lastValuesToCheck,
			Double greedyExploration)
			throws IOException {
		List<Double> lastNAverages = new ArrayList<Double>();
		List<Double> lastNProbabilities = new ArrayList<Double>();

		File outputDirectory = new File(DIRECTORY_FOR_LATEST_OUTPUT);
		outputDirectory.mkdir();

		int numberOfIterations = Integer.parseInt(ACRSTRUtil
				.getSetting("iterations"));

		String filename = DIRECTORY_FOR_LATEST_OUTPUT + '/'
				+ System.currentTimeMillis() + ".txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		parameters = new HashMap<String, String>();
		parameters.put("name", name);
		parameters.put("secondary-users", new Integer(numberOfSecondaryUsers).toString());
		parameters.put("primary-users", new Integer(numberOfPrimaryUsers).toString());
		parameters.put("spectra-number", new Integer(numberOfSpectra).toString());
		parameters.put("method", method.toString());
		parameters.put("evaluation", lastValuesToCheck.toString());
		parameters.put("q-response", qValueResponse.toString());
		parameters.put("rate-response", ratesResponse.toString());
		parameters.put("color", color);
		parameters.put("epsilon-decrement", epsilonDecrement.toString());
		parameters.put("greedy-exploration", greedyExploration.toString());
		parameters.put("pu-deactivation", Arrays.toString(EPOCHS_TO_DEACTIVATE_PUS));
		parameters.put("pu-introduction", Arrays.toString(EPOCHS_TO_INTRODUCE_PUS));
		String compared = ACRSTRUtil.getSetting("compare");
		String instance = parameters.get(compared);
		parameters.put("legend", getLegend(compared, instance));
		parameters.put("xLabel", X_AXIS_LABEL);
		parameters.put("yLabel", getYLabel(output));

		int numberOfLines = output.startsWith("average-") ? numberOfIterations
				/ TAKE_AVERAGE_OF_N_VALUES : numberOfIterations;

		parameters.put("numberOfValues", Integer.toString(numberOfLines));

		String jsonString = JSONValue.toJSONString(parameters);
		bw.write(jsonString + "\n");
		
		for (int j = 0; j < numberOfPrimaryUsers; j++) {
				String puName = String.format("PU%s", j + 1);
				environment.introduceAPU(puName);
		}

		for (int i = 0; i < environment.numberOfSecondaryUsers; i++) {
			environment.cognitiveRadios.add(new CognitiveRadio("CR" + (i + 1),
					environment, method, lastValuesToCheck, qValueResponse,
					ratesResponse, epsilonDecrement, greedyExploration));
		}

		for (int i = 0; i < numberOfIterations; i++) {
			
			for (int introductionEpoch : EPOCHS_TO_INTRODUCE_PUS) {
				if (i == introductionEpoch) {
					environment.introduceAPU("Newcomer PU");
				}
			}

			for (int deactivationEpoch : EPOCHS_TO_DEACTIVATE_PUS) {
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
					double average = sumOfLastNValues
							/ TAKE_AVERAGE_OF_N_VALUES;
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
					double average = sumOfLastNValues
							/ TAKE_AVERAGE_OF_N_VALUES;
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
		if (compared.equals("method") || compared.equals("q-response")
				|| compared.equals("epsilon-decrement") || compared.equals("rate-response")) {
			return instance;
		} else if (compared.equals("evaluation")) {
			int checkedLastValues = Integer.parseInt(instance);
			if (checkedLastValues > 0) {
				return String.format("Monitor Last %d Transmissions for Failure",
						checkedLastValues);
			} else {
				return "No Self Evaluation";
			}
		}
		return "Unknown";
	}

}
