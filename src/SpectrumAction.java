
public class SpectrumAction extends AbstractAction {

	Action action = Action.JUMP_SPECTRUM;
	
	Spectrum newSpectrum;
	
	public SpectrumAction(Spectrum aSpectrum) {
		newSpectrum = aSpectrum;
	}

	@Override
	public String toString() {
		return "SpectrumAction [action=" + action + ", newSpectrum="
				+ newSpectrum + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result =
				prime * result
						+ ((newSpectrum == null) ? 0 : newSpectrum.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;		
		if (this.compareTo((SpectrumAction) obj) == 0)
			return true;
		SpectrumAction other = (SpectrumAction) obj;
		if (action != other.action)
			return false;
		if (newSpectrum == null) {
			if (other.newSpectrum != null)
				return false;
		} else if (!newSpectrum.equals(other.newSpectrum))
			return false;
		return true;
	}

	@Override
	public int compareTo(AbstractAction arg0) {
		try {
			SpectrumAction o = (SpectrumAction) arg0;
			return newSpectrum.compareTo(o.newSpectrum);
		} catch (ClassCastException e) {
			try {
				@SuppressWarnings("unused")
				PowerAction o = (PowerAction) arg0;
				return 1;
			} catch (ClassCastException ex) {
				return 2;
			}
			
		}
	}
	
}
