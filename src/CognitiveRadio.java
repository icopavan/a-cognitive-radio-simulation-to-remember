import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

public class CognitiveRadio {

	public static final double INITIAL_EPSILON_VALUE = 0.8;
	public static final double INITIAL_LEARNING_RATE = 0.8;
	public static final double DISCOUNT_FACTOR = 0.8;
	public static final double MINIMUM_DOUBLE= - Double.MAX_VALUE;
	public static final double LEARNING_RATE_REDUCTION_FACTOR = 0.995;
	public static final double PROBABILITY_FOR_TRANSMISSION = 1.0;
	public static final double SPEED_OF_LIGHT = 3E8;
	public static final double PATH_LOSS_EXPONENT = - 2.0;
	public static final double DISTANCE = 5.0;
	public static final double CONSTANT_TO_INCREASE_RATES = 0.1;
	public static final double[] POWER_LEVELS = { 1000.0, 1250.0, 1500.0};
	public static final double PU_COLLISION_PENALTY = -15.0;
	public static final double CR_COLLISION_PENALTY = -5.0;
	public static final double PROBABILITY_FOR_CORRECT_SENSING = 1.0;
	public static final double SWITCHING_POWER_PER_CHANNEL = 0.5;
	public static final double SENSING_POWER = 50;

	public int iteration;
	public double energyConsumption;
	public double greedyExploration;
	public Environment environment;
	public Random randomGenerator;
	public State currentState;
	public String name;
	public int successfulTransmissions;
	public HashSet<StateAction> offendingQValues;
	public int unsuccessfulTransmissionsInARow;
	public int REWARD_HISTORY_SIZE = 10;
	public double epsilon;
	public double learningRate;
	public HashMap<StateAction, Double> Q;
	public HashMap<StateAction, Double> previousRewards;
	public HashMap<StateAction, CircularFifoBuffer> pastRewardsRepository;
	public double randomDouble;
	public Method method;
	public double currentIterationsReward;
	public int randomInt;
	public boolean isActiveThisIteration;
	public StateAction thisIterationsStateAction;
	public boolean isExploitingThisIteration;
	public int maximumUnsuccessfulTransmissionsTolerated;
	public RatesResponse responseForRates;
	public QValuesResponse responseForQValues;
	public double epsilonDecrement;
	public boolean changeProbabilities;
	public boolean successfullyTransmittedThisIteration;
	public List<TransmissionAction> possibleActions;
	public TransmissionAction actionTaken;
	
	public CognitiveRadio(String aName, Environment anEnvironment, Method aMethod,
			int checkLastNValues, QValuesResponse qValueResponse,
			RatesResponse ratesResponse, double decreaseEpsilonBy, double aGreedyExploration) {
		name = aName;
		environment = anEnvironment;
		offendingQValues = new HashSet<StateAction>();
		successfulTransmissions = 0;
		maximumUnsuccessfulTransmissionsTolerated = checkLastNValues;
		unsuccessfulTransmissionsInARow = 0;
		method = aMethod;
		Q = new HashMap<StateAction, Double>();
		epsilon = 0.8;
		learningRate = 0.8;
		greedyExploration = aGreedyExploration;
		responseForQValues = qValueResponse;
		responseForRates = ratesResponse;
		epsilonDecrement = decreaseEpsilonBy;
		randomGenerator = new Random();
		currentState = new State(0.0, 0.0);
		possibleActions = getPossibleActions();
		iteration = 0;
		previousRewards = new HashMap<StateAction, Double>();
		pastRewardsRepository = new HashMap<StateAction, CircularFifoBuffer>();
	}
	
	@Override
	public String toString() {
		return "CognitiveRadio [currentState=" + currentState + ", name="
				+ name + "]";
	}
	
	public List<TransmissionAction> getPossibleActions() {
		List<TransmissionAction> possibleActions = new ArrayList<TransmissionAction>();
		for (Spectrum spectrum : environment.spectra) {
			for (double transmissionPower : POWER_LEVELS) {
				possibleActions.add(new TransmissionAction(spectrum.frequency, transmissionPower));
			}
		}
		possibleActions.add(new TransmissionAction(0.0, 0.0));
		return possibleActions;
	}
	
	public void act() {
		iteration++;
		energyConsumption = 0.0;
		transmit();
		if (method == Method.QLEARNING) {
			learningRate *= LEARNING_RATE_REDUCTION_FACTOR;
			if (epsilon > epsilonDecrement) {
				epsilon -= epsilonDecrement;
			} else {
				epsilon = 0.0;
			}
		} else if (method == Method.GREEDY) {

		}
	}
	
	public boolean sense() {
		energyConsumption += SENSING_POWER;
		double randomDouble = randomGenerator.nextDouble();
		Spectrum currentSpectrum = getCurrentSpectrum();
		if (currentSpectrum == null) {
			return false;
		}
		if (randomDouble < PROBABILITY_FOR_CORRECT_SENSING) {
			return currentSpectrum.isUnderPUOccupation();
		} else {
			return !(currentSpectrum.isUnderPUOccupation());
		}
	}
	
	public Spectrum getCurrentSpectrum() {
		return environment.getChannel(currentState.frequency);
	}
	
	public void transmit() {
		isActiveThisIteration = false;
		isExploitingThisIteration = false;
		double randomDouble = randomGenerator.nextDouble();
		if (randomDouble < PROBABILITY_FOR_TRANSMISSION) {
			randomDouble = randomGenerator.nextDouble();
			if (method == Method.QLEARNING) {
				if (randomDouble < epsilon || Q.size() == 0) {
					explore();
				} else {
					exploit();
				}
			} else if (method == Method.RANDOM) {
				actionTaken = selectRandomAction();
			} else if (method == Method.GREEDY) {
				if (randomDouble < greedyExploration) {
					explore();
				} else {
					greedyExploit();
				}
			}
			State stateToSave = new State(currentState.frequency,
					currentState.transmissionPower);
			thisIterationsStateAction = new StateAction(stateToSave, actionTaken);
			conductAction(actionTaken);
		}
	}
	
	public void explore() {
		actionTaken = selectRandomAction();
	}
	
	public void exploit() {
		isExploitingThisIteration = true;
		actionTaken = getBestAction();
	}
	
	public void greedyExploit() {
		actionTaken = greedyBestAction();
	}
	
	public TransmissionAction getBestAction() {
		double maximumValue = MINIMUM_DOUBLE;
		TransmissionAction bestAction = null;
		for (TransmissionAction action : possibleActions) {
			StateAction possibleStateAction = new StateAction(currentState, action);
			if (Q.containsKey(possibleStateAction)) {
				if (Q.get(possibleStateAction) > maximumValue) {
					maximumValue = Q.get(possibleStateAction);
					bestAction = possibleStateAction.transmissionAction;
				}
			}
		}
		if (bestAction == null) {
			return selectRandomAction();
		}
		return bestAction;
	}
	
	public TransmissionAction selectRandomAction() {
		randomInt = randomGenerator.nextInt(possibleActions.size());
		return possibleActions.get(randomInt);
	}
	
	public TransmissionAction greedyBestAction() {
		double maximumValue = MINIMUM_DOUBLE;
		TransmissionAction bestAction = null;
		for (Map.Entry<StateAction, Double> previousAction : previousRewards.entrySet()) {
			if (previousAction.getValue() > maximumValue) {
				maximumValue = previousAction.getValue();
				bestAction = previousAction.getKey().transmissionAction;
			}
		}
		if (bestAction == null) {
			return selectRandomAction();
		}
		return bestAction;
	}
	
	public void conductAction(TransmissionAction action) {
		switchChannel(action.frequency);
		boolean isChannelOccupied = sense();
		if (isChannelOccupied) {
			currentState.transmissionPower = 0.0;
			isActiveThisIteration = false;
		} else {
			currentState.transmissionPower = action.transmissionPower;
			isActiveThisIteration = true;
		}
	}

	public void switchChannel(double aFrequency) {
		double frequencyDifference = Math.abs(currentState.frequency - aFrequency);
		energyConsumption += frequencyDifference * SWITCHING_POWER_PER_CHANNEL;
		if (currentState.frequency != 0.0) {
			Spectrum currentSpectrum = getCurrentSpectrum();
			currentSpectrum.occupyingAgents.remove(this);
		}
		if (aFrequency != 0.0) {
			Spectrum aSpectrum = environment.getChannel(aFrequency);
			currentState.frequency = aSpectrum.frequency;
			aSpectrum.occupyingAgents.add(this);
		}
	}
	
	public void evaluate() {
		currentIterationsReward = calculateReward();
		if (method == Method.QLEARNING) {
			if (maximumUnsuccessfulTransmissionsTolerated > 0) {
				if (isExploitingThisIteration) {
					if (!successfullyTransmittedThisIteration) {
						unsuccessfulTransmissionsInARow++;
						offendingQValues.add(thisIterationsStateAction);
					} else {
						unsuccessfulTransmissionsInARow = 0;
						offendingQValues.clear();
					}
					if (unsuccessfulTransmissionsInARow > maximumUnsuccessfulTransmissionsTolerated) {
						unsuccessfulTransmissionsInARow = 0;
						if (maximumUnsuccessfulTransmissionsTolerated > 0) {
							if (responseForRates == RatesResponse.RESET_TO_INITIAL_VALUES) {
								epsilon = INITIAL_EPSILON_VALUE;
								learningRate = INITIAL_LEARNING_RATE;
							} else if (responseForRates == RatesResponse.SET_TO_MIDPOINT) {
								epsilon += (INITIAL_EPSILON_VALUE - epsilon) / 2.0;
								learningRate += (INITIAL_LEARNING_RATE - learningRate) / 2.0;
							} else if (responseForRates == RatesResponse.INCREASE_BY_CONSTANT) {
								epsilon += CONSTANT_TO_INCREASE_RATES;
								if (epsilon > 0.8) {
									epsilon = 0.8;
								}
								learningRate += CONSTANT_TO_INCREASE_RATES;
								if (learningRate > 0.8) {
									learningRate = 0.8;
								}
							}
							if (responseForQValues == QValuesResponse.DELETE_OBSOLETE_VALUES) {
								for (StateAction offendingStateAction : offendingQValues) {
									Q.remove(offendingStateAction);
								}
							} else if (responseForQValues == QValuesResponse.DELETE_ALL_VALUES) {
								Q.clear();
							} else if (responseForQValues == QValuesResponse.KEEP_ALL_VALUES) {
								// Do nothing
							}
						}
					}
				}
			}
			updateQ(thisIterationsStateAction, currentIterationsReward);
		} else if (method == Method.GREEDY) {
			updateGreedyHistory(thisIterationsStateAction, currentIterationsReward);
		}
	}
	
	public double calculateReward() {
		double reward, energyEfficiency = 0.0;
		boolean puCollision = false, crCollision = false,
				successfulTransmission = false;
		Spectrum currentSpectrum = environment.getChannel(currentState.frequency);
		if (currentSpectrum != null && currentSpectrum.occupyingPU != null) {
			puCollision = true;
		}
		if (currentSpectrum != null && isThereCRCollision()) {
			crCollision = true;
		}
		if (!puCollision && !crCollision && currentState.frequency != 0.0 &&
				currentState.transmissionPower != 0.0) {
			successfulTransmission = true;
			if (energyConsumption > 0) {
				energyEfficiency = currentState.transmissionPower / energyConsumption;	
			}
		}
		successfullyTransmittedThisIteration = successfulTransmission;
		reward = successfulTransmission ? energyEfficiency : -energyConsumption;
		return reward;
	}
	
	public boolean isThereCRCollision() {
		Spectrum currentSpectrum = getCurrentSpectrum();
		for (CognitiveRadio cr : currentSpectrum.occupyingAgents) {
			if (!this.equals(cr) && cr.isActiveThisIteration) {
				return true;
			}
		}
		return false;
	}
	
	public void updateQ(StateAction stateAction, double reward) {
		if (Q.containsKey(stateAction)) {
			double oldValue = Q.get(stateAction);
			double valueUpdate = learningRate * (reward + DISCOUNT_FACTOR * maxQ()
					- oldValue);
			double newValue = oldValue + valueUpdate;
			Q.put(stateAction, newValue);
		} else {
			Q.put(stateAction, reward);
		}
	}
	
	public double maxQ() {
		List<StateAction> allStateActions = new ArrayList<StateAction>();
		for (TransmissionAction possibleAction : possibleActions) {
			allStateActions.add(new StateAction(currentState, possibleAction));
		}
		List<StateAction> stateActionsWithPolicies = new ArrayList<StateAction>();
		for (StateAction stateAction : allStateActions) {
			if (Q.containsKey(stateAction)) {
				stateActionsWithPolicies.add(stateAction);
			}
		}
		if (stateActionsWithPolicies.size() == 0) {
			return 0;
		}
		double maximumValue = MINIMUM_DOUBLE;
		
		for (StateAction stateAction : stateActionsWithPolicies) {
			if (Q.get(stateAction) > maximumValue) {
				maximumValue = Q.get(stateAction);
			}
		}
		return maximumValue;
	}
	
	public void updateGreedyHistory(StateAction aStateAction, Double aReward) {
		previousRewards.put(aStateAction, aReward);
	}
	
}
