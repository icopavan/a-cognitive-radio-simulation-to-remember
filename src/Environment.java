import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Environment {

	public static final double[] AVAILABLE_SPECTRA = { 125E6, 250E6, 500E6, 750E6, 1000E6 };
	public List<Spectrum> spectra;
	public List<CognitiveRadio> cognitiveRadios;
	public int numberOfSecondaryUsers;
	public List<PrimaryUser> primaryUsers;
	public List<Spectrum> channelsOccupiedByPUs;
	public List<Spectrum> channelsWithSpectrumHoles;
	public Random randomNumberGenerator;
	
	public Environment(int aNumberForSecondaryUsers) {
		spectra = new ArrayList<Spectrum>();
		for (double frequency : AVAILABLE_SPECTRA) {
			spectra.add(new Spectrum(frequency));
		}
		cognitiveRadios = new ArrayList<CognitiveRadio>();
		primaryUsers = new ArrayList<PrimaryUser>();
		numberOfSecondaryUsers = aNumberForSecondaryUsers;
		channelsOccupiedByPUs = new ArrayList<Spectrum>();
		channelsWithSpectrumHoles = new ArrayList<Spectrum>();
		channelsWithSpectrumHoles.addAll(spectra);
		randomNumberGenerator = new Random();
	}
	
	public Spectrum getChannel(double aFrequency) {
		for (Spectrum s : spectra) {
			if (s.frequency == aFrequency) {
				return s;
			}
		}
		return null;
	}
	
	public void introduceAPU(String puName) {
		PrimaryUser transmitterPU = new PrimaryUser(puName, this);
		int randomInt = randomNumberGenerator.nextInt(channelsWithSpectrumHoles.size());
		Spectrum aSpectrum = channelsWithSpectrumHoles.get(randomInt);
		aSpectrum.getOccupiedByPU(transmitterPU);
		channelsOccupiedByPUs.add(aSpectrum);
		channelsWithSpectrumHoles.remove(aSpectrum);
		primaryUsers.add(transmitterPU);
	}
	
	public void deactivateAPU() {
		Spectrum spectrumToVacate = null;
		PrimaryUser primaryUserToDeactivate = null;
		for (Spectrum s : channelsOccupiedByPUs) {
			primaryUserToDeactivate = s.occupyingPU;
			s.getVacatedByPU();
			spectrumToVacate = s;
		}
		channelsOccupiedByPUs.remove(spectrumToVacate);
		primaryUsers.remove(primaryUserToDeactivate);
	}	

}
