import java.util.ArrayList;
import java.util.List;

public class Environment {

	public int numberOfSpectra;
	
	public double[] availableSpectrums = { 125E6, 250E6, 500E6, 750E6, 1000E6 };
	public List<Spectrum> spectrums;
	public List<CognitiveRadio> cognitiveRadios;
	public int numberOfSecondaryUsers;
	public List<PrimaryUser[]> primaryUserPairs;
	
	public Environment() {
		numberOfSpectra = availableSpectrums.length;
		spectrums = new ArrayList<Spectrum>();
		for (int i = 0; i < numberOfSpectra; i++) {
			spectrums.add(new Spectrum(availableSpectrums[i]));
		}
		cognitiveRadios = new ArrayList<CognitiveRadio>();
		primaryUserPairs = new ArrayList<PrimaryUser[]>();
		numberOfSecondaryUsers = Integer.parseInt(ACRSTRUtil.getSetting("secondary-users"));
	}
	
	public Spectrum getChannel(double aFrequency) {
		for (Spectrum s : spectrums) {
			if (s.frequency == aFrequency) {
				return s;
			}
		}
		return null;
	}
	
}
