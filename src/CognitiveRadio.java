import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

public class CognitiveRadio extends Agent {

	public static final double INITIAL_EPSILON_VALUE = 0.8;
	public static final double INITIAL_LEARNING_RATE = 0.8;
	public static final double DISCOUNT_FACTOR = 0.8;
	public static final double MINIMUM_DOUBLE= - Double.MAX_VALUE;
	public static final double LEARNING_RATE_REDUCTION_FACTOR = 0.995;
	public static final double INITIAL_PROBABILITY_FOR_TRANSMISSION = 0.2;
	public static final double SPEED_OF_LIGHT = 3E8;
	public static final double PATH_LOSS_EXPONENT = - 2.0;
	public static final double DISTANCE = 5.0;
	public static final double RECEIVER_THRESHOLD = 1E-8;
	public static final double[] DISTANCES = { 1.0, 1.41, 2.0, 2.82, 3.0, 4.24 };
	public static final double CONSTANT_TO_INCREASE_RATES = 0.1;
	public static final double PROBABILITY_CHANGE_STEP = 0.1;
	
	public int successfulTransmissions;
	
	public HashSet<StateAction> offendingQValues;
	
	public CircularFifoBuffer rewardHistory;
	
	public int negativeRewardsInARow;

	public int REWARD_HISTORY_SIZE = 10;
	
	public double epsilon;
	
	public AbstractAction actionTaken;
	
	public List<AbstractAction> possibleActions;
	
	public double learningRate;
	
	public HashMap<StateAction, Double> Q;
	
	public double randomDouble;
	
	public Method method;
	
	public double currentIterationsReward;
	
	public int randomInt;

	public NothingAction nothingActionForComparison;
	
	public boolean changedChannelThisIteration;
	
	public boolean isActiveThisIteration;
	
	public StateAction thisIterationsStateAction;
	
	public boolean succesfullyTransmittedThisIteration;
	
	public boolean isExploitingThisIteration;
	
	public int maximumNumberOfNegativeValuesTolerated;
	
	public RatesResponse responseForRates;
	
	public QValuesResponse responseForQValues;
	
	public double epsilonDecrement;
	
	public double probabilityForTransmission;
	
	public boolean changeProbabilities;
	
	public CognitiveRadio(String name, Environment environment, Method aMethod,
			int checkLastNValues, QValuesResponse qValueResponse,
			RatesResponse ratesResponse, double decreaseEpsilonBy, boolean changeProbability) {
		super(name, environment);
		offendingQValues = new HashSet<StateAction>();
		successfulTransmissions = 0;
		maximumNumberOfNegativeValuesTolerated = checkLastNValues;
		negativeRewardsInARow = 0;
		method = aMethod;
		Q = new HashMap<StateAction, Double>();
		epsilon = 0.8;
		learningRate = 0.8;
		nothingActionForComparison = new NothingAction();
		rewardHistory = new CircularFifoBuffer(REWARD_HISTORY_SIZE);
		responseForQValues = qValueResponse;
		responseForRates = ratesResponse;
		epsilonDecrement = decreaseEpsilonBy;
		probabilityForTransmission = INITIAL_PROBABILITY_FOR_TRANSMISSION;
		changeProbabilities = changeProbability;
	}
	
	public void occupyChannel(Spectrum aSpectrum) {
		currentState.spectrum = aSpectrum;
		aSpectrum.occupyingAgents.add(this);
	}
	
	public void vacateChannel() {
		currentState.spectrum.occupyingAgents.remove(this);
		currentState.spectrum = null;
	}
	
	public void initializeParameters() {
		chooseSpectrum();
	}
	
	public void iterate() {
		randomGenerator = new Random();
		if (role == Role.TRANSMITTER) {
			transmit();
		} else {
			receive();
		}
		if (method == Method.QLEARNING) {
			learningRate *= LEARNING_RATE_REDUCTION_FACTOR;
			if (epsilon > epsilonDecrement) {
				epsilon -= epsilonDecrement;
			}
		}
	}
	
	public void jumpSpectrum(SpectrumAction aSpectrumAction) {
		vacateChannel();
		occupyChannel(aSpectrumAction.newSpectrum);
		
		changedChannelThisIteration = true;
	}
	
	public void conductAction(AbstractAction abstractAction) {
		Action chosenAction = determineAction(abstractAction);
		if (chosenAction == Action.JUMP_SPECTRUM) {
			jumpSpectrum((SpectrumAction) abstractAction);
		} else if (chosenAction == Action.JUMP_PROBABILITY) {
			jumpProbability((ProbabilityAction) abstractAction);
		}
	}
	
	public void jumpProbability(ProbabilityAction aProbabilityAction) {
		if (aProbabilityAction.probabilityChange == ProbabilityChange.DECREMENT) {
			if (probabilityForTransmission > PROBABILITY_CHANGE_STEP) {
				probabilityForTransmission -= PROBABILITY_CHANGE_STEP;
			}
		} else if (aProbabilityAction.probabilityChange == ProbabilityChange.INCREMENT) {
			if (probabilityForTransmission < 1 - PROBABILITY_CHANGE_STEP) {
				probabilityForTransmission += PROBABILITY_CHANGE_STEP;
			}
		}
	}
	
	@Override
	public void transmit() {
		isActiveThisIteration = false;
		isExploitingThisIteration = false;
		changedChannelThisIteration = false;
		double randomDouble = randomGenerator.nextDouble();
		if (randomDouble < probabilityForTransmission) {
			super.transmit();
			isActiveThisIteration = true;
			State stateToSave = new State(currentState.spectrum);
			previousState = new State(currentState.spectrum);
			// Explore if random number is less than epsilon or there is no policy yet
			randomDouble = randomGenerator.nextDouble();
			if (method == Method.QLEARNING) {
				if (randomDouble < epsilon || Q.size() == 0) {
					explore();
				} else {
					exploit();
				}
			} else if (method == Method.RANDOM) {
				actionTaken = selectRandomAction();
			} 
			conductAction(actionTaken);
			thisIterationsStateAction = new StateAction(stateToSave, actionTaken);
		}
		if (debug) {
			ACRSTRUtil.log("End of iteration " + iterationNumber);
			printQ();
		}
	}
	
	// Respond to environmental changes
	public void evaluate() {
		currentIterationsReward = calculateReward();
		rewardHistory.add(new Double(currentIterationsReward));
		if (isExploitingThisIteration) {
			if (currentIterationsReward < 0.0) {
				negativeRewardsInARow++;
				offendingQValues.add(thisIterationsStateAction);
			} else {
				negativeRewardsInARow = 0;
				offendingQValues.clear();
			}
			if (negativeRewardsInARow > maximumNumberOfNegativeValuesTolerated) {
				negativeRewardsInARow = 0;
				if (maximumNumberOfNegativeValuesTolerated > 0) {
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
		updateQ(thisIterationsStateAction, currentIterationsReward);
	}

	@Override
	public void receive() {
		currentState.spectrum = peer.currentState.spectrum;
		super.receive();
	}

	/**
	 * Updates Q with the state action saved before taking an action and the reward obtained after 
	 * having taken the action
	 * 
	 */
	public void updateQ(StateAction stateAction, double reward) {
		if (Q.containsKey(stateAction)) {
			double oldValue = Q.get(stateAction);
			double valueUpdate = learningRate * (reward + DISCOUNT_FACTOR * maxQ() - oldValue);
			double newValue = oldValue + valueUpdate;
			Q.put(stateAction, newValue);
		} else {
			Q.put(stateAction, reward);
		}
	}
		
	public List<AbstractAction> getPossibleActions() {
		possibleActions = new ArrayList<AbstractAction>();
		for (Spectrum availableSpectrum : environment.spectrums) {
			if (currentState.spectrum.containsPrimaryUser
					&& !availableSpectrum.equals(currentState.spectrum)
					&& !availableSpectrum.containsPrimaryUser) {
				possibleActions.add(new SpectrumAction(availableSpectrum));
			}
		}
		possibleActions.add(new NothingAction());
		if (changeProbabilities) {
			possibleActions.add(new ProbabilityAction(ProbabilityChange.DECREMENT));
			possibleActions.add(new ProbabilityAction(ProbabilityChange.INCREMENT));
		}
		return possibleActions;
	}
		
	/**
	 * Calculates maximum Q values for the newly attained state 
	 */
	public double maxQ() {
		// Construct all possible state actions from the current state
		List<StateAction> allStateActions = new ArrayList<StateAction>();
		possibleActions = getPossibleActions();
		for (AbstractAction possibleAction : possibleActions) {
			allStateActions.add(new StateAction(currentState, possibleAction));
		}
		List<StateAction> stateActionsWithPolicies = new ArrayList<StateAction>();
		for (StateAction stateAction : allStateActions) {
			if (Q.containsKey(stateAction)) {
				stateActionsWithPolicies.add(stateAction);
			}
		}
		// Return 0 if there is no policy yet for any of the possible actions
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
	
	public void explore() {
		if (debug) {
			ACRSTRUtil.log(name + " is exploring.");
		}
		randomInt = Math.abs(randomGenerator.nextInt());
		if (randomInt % 2 == 0) {
			changeSpectrum();
		} else {
			doNothing();
		}
		
	}
	
	public void printQ() {
		ACRSTRUtil.log("=====");
		ACRSTRUtil.log("Q for " + name);
		ACRSTRUtil.log("-----");
		Map<StateAction, Double> orderedQ = new TreeMap<StateAction, Double>();
		orderedQ.putAll(Q);
		Iterator<Entry<StateAction, Double>> iter = orderedQ.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<StateAction, Double> pairs = (Map.Entry<StateAction, Double>) iter.next();
			ACRSTRUtil.log("[State Action Pair: " + pairs.getKey().toString() + ", Q: " + pairs.getValue() + "]");
		}
		ACRSTRUtil.log("+++++++++");
	}
	
	public void changeSpectrum() {
		if (debug) {
			ACRSTRUtil.log(name + " is changing spectrum.");
		}
		randomInt = randomGenerator.nextInt(environment.numberOfSpectra);
		Spectrum randomSpectrum = environment.spectrums.get(randomInt);
		while (currentState.spectrum.equals(randomSpectrum)) {
			randomInt = randomGenerator.nextInt(environment.numberOfSpectra);
			randomSpectrum = environment.spectrums.get(randomInt);
		}
		
		currentState.spectrum = randomSpectrum;
		actionTaken = new SpectrumAction(currentState.spectrum);
	}
	
	public void doNothing() {
		actionTaken = new NothingAction();
	}
	
	/**
	 * Sets actionToConduct to the best action according to current Q. 
	 */
	public void exploit() {
		isExploitingThisIteration = true;
		if (debug) {
			ACRSTRUtil.log(name + " is exploiting.");
		}
		actionTaken = getBestAction();
		if (debug) {
			ACRSTRUtil.log(name + " decided best action to be " + actionTaken + ".");
		}
	}
	
	public AbstractAction getBestAction() {
		double maximumValue = MINIMUM_DOUBLE;
		AbstractAction bestAction = null;
		possibleActions = getPossibleActions();
		for (AbstractAction action : possibleActions) {
			StateAction possibleStateAction = new StateAction(currentState, action);
			if (Q.containsKey(possibleStateAction)) {
				if (Q.get(possibleStateAction) > maximumValue) {
					maximumValue = Q.get(possibleStateAction);
					bestAction = possibleStateAction.action;
				}
			}
		}
		if (bestAction == null) {
			return new NothingAction();
		}
		return bestAction;
	}
	
	public AbstractAction selectRandomAction() {
		randomInt = Math.abs(randomGenerator.nextInt());
		if (randomInt % 3 == 0) {
			int randomInt = randomGenerator.nextInt(environment.numberOfSpectra);
			Spectrum newSpectrum = environment.spectrums.get(randomInt);
			randomInt = randomGenerator.nextInt(environment.numberOfSpectra);
			newSpectrum = environment.spectrums.get(randomInt);
			return new SpectrumAction(newSpectrum);
		} else if (randomInt % 3 == 1) {
			int randomInt = randomGenerator.nextInt(2);
			ProbabilityAction probabilityAction = null;
			if (randomInt == 0) {
				probabilityAction = new ProbabilityAction(ProbabilityChange.DECREMENT);
			} else if (randomInt == 1) {
				probabilityAction = new ProbabilityAction(ProbabilityChange.INCREMENT);
			}
			return probabilityAction;
		} else {
			return new NothingAction();
		}
	}
	
	public Action determineAction(AbstractAction action) {
		String className = action.getClass().getSimpleName();
		if (className.equals("PowerAction")) {
			return Action.JUMP_POWER;
		} else if (className.equals("SpectrumAction")) {
			return Action.JUMP_SPECTRUM;
		} else if (className.equals("ProbabilityAction")) {
			return Action.JUMP_PROBABILITY;
		} else {
			return Action.DO_NOTHING;
		}
	}

	public boolean isThereCRCollision() {
		for (CognitiveRadio cr : currentState.spectrum.occupyingAgents) {
			if (!this.equals(cr) && cr.isActiveThisIteration) {
				return true;
			}
		}
		return false;
	}
	
	public double calculateReward() {
		double reward;
		succesfullyTransmittedThisIteration = false;
		if (currentState.spectrum.containsPrimaryUser) {
			reward = -15.0;
		} else if (isThereCRCollision()){
			reward = -5.0;
		} else {
			reward = 5.0;
			succesfullyTransmittedThisIteration = true;
			successfulTransmissions++;
		}
		if (debug) {
			ACRSTRUtil.log(name + " got reward: " + reward);
		}
		return reward;
	}

	
	
	public boolean gettingPositiveRewards() {
		double totalReward = 0.0;
		@SuppressWarnings("unchecked")
		Iterator<Double> iter = rewardHistory.iterator();
		while (iter.hasNext()) {
			totalReward += iter.next();
		}
		double average = totalReward / REWARD_HISTORY_SIZE;
		return average > 0.0;
	}
}
