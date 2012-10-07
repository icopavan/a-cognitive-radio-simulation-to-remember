import java.util.ArrayList;
import java.util.List;

public class Environment {

	public int numberOfSpectra;
	public int numberOfPowerLevels;
	
	public double[] powerLevels = { 0.5E-3, 1.0E-3, 2.0E-3, 4.0E-3 };
	public double[] availableSpectrums = { 50E6, 500E6, 2000E6, 5000E6};
	public List<Spectrum> spectrums;
	public List<CognitiveRadio> cognitiveRadios;
	public int numberOfSecondaryUsers;
	
	public Environment() {
		numberOfSpectra = availableSpectrums.length;
		numberOfPowerLevels = powerLevels.length;
		spectrums = new ArrayList<Spectrum>();
		for (int i = 0; i < numberOfSpectra; i++) {
			spectrums.add(new Spectrum(availableSpectrums[i]));
		}
		cognitiveRadios = new ArrayList<CognitiveRadio>();
		numberOfSecondaryUsers = Integer.parseInt(FeliceUtil.getSetting("secondary-users"));
	}
	
	public void printQValues() {
		for (CognitiveRadio cr : cognitiveRadios) {
			cr.printQ();
		}
	}
	
	public void printStates() {
		for (CognitiveRadio cr: cognitiveRadios) {
			FeliceUtil.log(cr.name + " " + cr.currentState + " " + cr.actionTaken);
		}
	}
	
}
