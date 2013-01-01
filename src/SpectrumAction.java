
public class SpectrumAction extends AbstractAction {

	Spectrum newSpectrum;
	
	public SpectrumAction(Spectrum aSpectrum) {
		newSpectrum = aSpectrum;
	}

	@Override
	public String toString() {
		return "SpectrumAction [newSpectrum=" + newSpectrum + "]";
	}

}
