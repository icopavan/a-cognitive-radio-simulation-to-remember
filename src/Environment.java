import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Environment {

	public static final double SPECTRA_STEP_SIZE = 20.0;
	public static final double STARTING_SPECTRUM = 1600.0;
	public List<Spectrum> spectra;
	public List<CognitiveRadio> cognitiveRadios;
	public int numberOfSecondaryUsers;
	public int numberOfSpectra;
	public List<PrimaryUser> primaryUsers;
	public List<Spectrum> channelsOccupiedByPUs;
	public List<Spectrum> channelsWithSpectrumHoles;
	public Random randomNumberGenerator;
	
	public Environment(int aNumberForSecondaryUsers, int aNumberForSpectra) {
		numberOfSpectra = aNumberForSpectra;
		spectra = new ArrayList<Spectrum>();
		for (int i = 0; i < numberOfSpectra; i++) {
			spectra.add(new Spectrum(STARTING_SPECTRUM + i * SPECTRA_STEP_SIZE));
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
		PrimaryUser aPU = new PrimaryUser(puName, this);
		int randomInt = randomNumberGenerator.nextInt(channelsWithSpectrumHoles.size());
		Spectrum aSpectrum = channelsWithSpectrumHoles.get(randomInt);
		aSpectrum.getOccupiedByPU(aPU);
		channelsOccupiedByPUs.add(aSpectrum);
		channelsWithSpectrumHoles.remove(aSpectrum);
		primaryUsers.add(aPU);
	}
	
	public void deactivateAPU() {
		int randomInt = randomNumberGenerator.nextInt(channelsOccupiedByPUs.size());
		Spectrum spectrumToVacate = channelsOccupiedByPUs.get(randomInt);
		PrimaryUser primaryUserToDeactivate = spectrumToVacate.occupyingPU;
		spectrumToVacate.getVacatedByPU();
		channelsOccupiedByPUs.remove(spectrumToVacate);
		channelsWithSpectrumHoles.add(spectrumToVacate);
		primaryUsers.remove(primaryUserToDeactivate);
	}	

}
